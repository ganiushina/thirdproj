package ru.alta.hhdictdownloader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Executes download workflow for configured endpoints and hierarchical area dictionaries.
 */
final class DictionaryDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryDownloader.class);
    private static final Pattern SLASH_PATTERN = Pattern.compile("/+");

    private final Config config;
    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    DictionaryDownloader(Config config) {
        this.config = config;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    void run() throws IOException, InterruptedException {
        Path outputDir = Files.createDirectories(config.outputDir());
        LOGGER.info("Saving responses to {}", outputDir.toAbsolutePath());

        Deque<String> queue = new ArrayDeque<>(config.endpoints());
        Set<String> scheduled = new LinkedHashSet<>(queue);
        Set<String> processed = new HashSet<>();

        while (!queue.isEmpty()) {
            String endpoint = queue.removeFirst();
            if (!processed.add(endpoint)) {
                continue;
            }
            HttpResponse<String> response = executeRequest(endpoint);
            if (response == null) {
                continue;
            }

            saveResponse(outputDir, endpoint, response.body());
            if (endpoint.startsWith("/areas")) {
                enqueueAreaChildren(response.body(), queue, scheduled, processed);
            }
        }
    }

    private HttpResponse<String> executeRequest(String endpoint) {
        String url = config.baseUrl() + endpoint;
        LOGGER.info("Fetching {}", url);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + config.token())
                .timeout(Duration.ofSeconds(60))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int code = response.statusCode();
            if (code >= 200 && code < 300) {
                return response;
            }
            LOGGER.error("Failed to fetch {} (HTTP {}): {}", endpoint, code, truncate(response.body()));
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Request failed for {}", endpoint, e);
        }
        return null;
    }

    private void saveResponse(Path outputDir, String endpoint, String body) {
        String fileName = createFileName(endpoint);
        Path file = outputDir.resolve(fileName);
        try {
            Files.writeString(file, body, StandardCharsets.UTF_8);
            LOGGER.info("Saved {} bytes to {}", body.length(), file.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Failed to write {}", file, e);
        }
    }

    private void enqueueAreaChildren(String body, Deque<String> queue, Set<String> scheduled, Set<String> processed) {
        try {
            JsonNode root = mapper.readTree(body);
            Set<String> ids = new LinkedHashSet<>();
            collectAreaIds(root, ids);
            List<String> newEndpoints = new ArrayList<>();
            for (String id : ids) {
                String next = "/areas/" + id;
                if (!processed.contains(next) && scheduled.add(next)) {
                    newEndpoints.add(next);
                }
            }
            if (!newEndpoints.isEmpty()) {
                LOGGER.info("Discovered {} new area endpoints", newEndpoints.size());
                queue.addAll(newEndpoints);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to parse area payload", e);
        }
    }

    private void collectAreaIds(JsonNode node, Set<String> collector) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            JsonNode idNode = node.get("id");
            if (idNode != null && idNode.isValueNode()) {
                collector.add(idNode.asText());
            }
            JsonNode areasNode = node.get("areas");
            if (areasNode != null) {
                collectAreaIds(areasNode, collector);
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                collectAreaIds(element, collector);
            }
        }
    }

    private static String createFileName(String endpoint) {
        String sanitized = endpoint;
        if (sanitized.startsWith("/")) {
            sanitized = sanitized.substring(1);
        }
        sanitized = sanitized.isEmpty() ? "root" : sanitized;
        sanitized = SLASH_PATTERN.matcher(sanitized).replaceAll("_");
        return sanitized + ".txt";
    }

    private static String truncate(String body) {
        if (body == null) {
            return "<no body>";
        }
        String trimmed = body.strip();
        if (trimmed.length() <= 240) {
            return trimmed;
        }
        return trimmed.substring(0, 237) + "...";
    }
}
