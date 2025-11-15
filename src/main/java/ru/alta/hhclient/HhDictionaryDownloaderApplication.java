package ru.alta.hhclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootApplication
public class HhDictionaryDownloaderApplication {

    private static final String API_BASE_URL = "https://api.hh.ru";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        SpringApplication.run(HhDictionaryDownloaderApplication.class, args);
    }

    @Bean
    public CommandLineRunner downloadDictionaries() {
        return args -> {
            DownloaderOptions options = DownloaderOptions.fromArgs(args);
            Path outputDirectory = prepareOutputDirectory(options);

            Map<String, String> endpoints = buildEndpoints(options);
            RestTemplate restTemplate = new RestTemplate();

            Queue<Map.Entry<String, String>> queue = new ArrayDeque<>(endpoints.entrySet());
            Set<String> scheduled = new LinkedHashSet<>(endpoints.keySet());
            Set<String> processed = new LinkedHashSet<>();

            while (!queue.isEmpty()) {
                Map.Entry<String, String> entry = queue.poll();
                String endpointName = entry.getKey();
                String url = entry.getValue();

                if (!processed.add(endpointName)) {
                    continue;
                }

                try {
                    System.out.printf("Fetching %s ...%n", url);
                    ResponseEntity<String> response = restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            new HttpEntity<>(buildHeaders(options)),
                            String.class
                    );

                    Path outputFile = outputDirectory.resolve(toFileName(endpointName) + ".txt");
                    Files.writeString(outputFile, response.getBody(), StandardCharsets.UTF_8);
                    System.out.printf("Saved %s%n", outputFile.toAbsolutePath());

                    Collection<Map.Entry<String, String>> hierarchicalEndpoints = discoverHierarchicalEndpoints(endpointName, response.getBody());
                    for (Map.Entry<String, String> hierarchicalEntry : hierarchicalEndpoints) {
                        if (scheduled.add(hierarchicalEntry.getKey())) {
                            queue.add(hierarchicalEntry);
                        }
                    }
                } catch (RestClientException | IOException e) {
                    System.err.printf("Failed to fetch %s: %s%n", url, e.getMessage());
                }
            }
        };
    }

    private static Collection<Map.Entry<String, String>> discoverHierarchicalEndpoints(String endpointName, String responseBody) {
        if ("areas".equals(endpointName)) {
            return buildAreaEndpoints(responseBody);
        }

        return List.of();
    }

    private static Collection<Map.Entry<String, String>> buildAreaEndpoints(String responseBody) {
        List<Map.Entry<String, String>> endpoints = new ArrayList<>();
        try {
            List<String> areaIds = extractAreaIds(responseBody);
            for (String areaId : areaIds) {
                String childEndpoint = "areas/" + areaId;
                endpoints.add(new AbstractMap.SimpleEntry<>(childEndpoint, API_BASE_URL + "/" + childEndpoint));
            }
        } catch (JsonProcessingException e) {
            System.err.printf("Failed to parse areas hierarchy: %s%n", e.getOriginalMessage());
        }
        return endpoints;
    }

    private static List<String> extractAreaIds(String responseBody) throws JsonProcessingException {
        JsonNode root = OBJECT_MAPPER.readTree(responseBody);
        Set<String> ids = new LinkedHashSet<>();

        if (root.isArray()) {
            for (JsonNode node : root) {
                collectAreaIds(node, ids);
            }
        } else if (root.isObject()) {
            collectAreaIds(root, ids);
        }

        return new ArrayList<>(ids);
    }

    private static void collectAreaIds(JsonNode node, Collection<String> accumulator) {
        JsonNode idNode = node.get("id");
        if (idNode != null && idNode.isValueNode()) {
            accumulator.add(idNode.asText());
        }

        JsonNode areasNode = node.get("areas");
        if (areasNode != null && areasNode.isArray()) {
            for (JsonNode child : areasNode) {
                collectAreaIds(child, accumulator);
            }
        }
    }

    private static String toFileName(String endpointName) {
        return endpointName.replace('/', '_');
    }

    private static Map<String, String> buildEndpoints(DownloaderOptions options) {
        Map<String, String> endpoints = new LinkedHashMap<>();

        List<String> endpointPaths = options.getCustomEndpoints().isEmpty()
                ? List.of(
                "/areas",
                "/industries",
                "/professional_roles",
                "/specializations",
                "/languages",
                "/dictionaries",
                "/metro",
                "/locales",
                "/vacancy_branded_templates",
                "/vacancy_dictionaries",
                "/skills",
                "/currency"
        )
                : options.getCustomEndpoints();

        endpointPaths.stream()
                .map(path -> path.startsWith("/") ? path : "/" + path)
                .forEach(path -> endpoints.put(path.substring(1), API_BASE_URL + path));

        return endpoints;
    }

    private static HttpHeaders buildHeaders(DownloaderOptions options) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(options.getToken());
        headers.set(HttpHeaders.ACCEPT, "application/json");
        return headers;
    }

    private static Path prepareOutputDirectory(DownloaderOptions options) throws IOException {
        Path baseOutputDirectory = options.getOutputDirectory();
        if (options.isTimestamped()) {
            String suffix = TIMESTAMP_FORMATTER.format(LocalDateTime.now());
            baseOutputDirectory = baseOutputDirectory.resolve("hh-dictionaries-" + suffix);
        }
        Files.createDirectories(baseOutputDirectory);
        return baseOutputDirectory;
    }

    private static final class DownloaderOptions {
        private static final String DEFAULT_OUTPUT = "hh-dictionaries";
        private static final String DEFAULT_TOKEN = "12345";

        private final Path outputDirectory;
        private final boolean timestamped;
        private final String token;
        private final List<String> customEndpoints;

        private DownloaderOptions(Path outputDirectory, boolean timestamped, String token, List<String> customEndpoints) {
            this.outputDirectory = outputDirectory;
            this.timestamped = timestamped;
            this.token = token;
            this.customEndpoints = customEndpoints;
        }

        public Path getOutputDirectory() {
            return outputDirectory;
        }

        public boolean isTimestamped() {
            return timestamped;
        }

        public String getToken() {
            return token;
        }

        public List<String> getCustomEndpoints() {
            return customEndpoints;
        }

        public static DownloaderOptions fromArgs(String[] args) {
            Path outputDir = Paths.get(DEFAULT_OUTPUT);
            boolean timestamped = false;
            String token = DEFAULT_TOKEN;
            List<String> endpoints = List.of();

            for (String arg : args) {
                if (arg.startsWith("--output=")) {
                    outputDir = Paths.get(arg.substring("--output=".length()));
                } else if (arg.equals("--timestamp")) {
                    timestamped = true;
                } else if (arg.startsWith("--token=")) {
                    token = arg.substring("--token=".length());
                } else if (arg.startsWith("--endpoints=")) {
                    endpoints = List.of(arg.substring("--endpoints=".length()).split(","))
                            .stream()
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                }
            }

            return new DownloaderOptions(outputDir, timestamped, token, endpoints);
        }
    }
}
