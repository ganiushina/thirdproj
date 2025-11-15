package ru.alta.hhdictdownloader;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Configuration parsed from command-line arguments.
 */
final class Config {
    static final String DEFAULT_BASE_URL = "https://api.hh.ru";
    static final String DEFAULT_TOKEN = "12345";

    private static final List<String> DEFAULT_ENDPOINTS = List.of(
            "/areas",
            "/dictionaries",
            "/industries",
            "/professional_roles",
            "/specializations",
            "/languages",
            "/language_levels",
            "/employers",
            "/locales",
            "/clusters",
            "/agency_commissions",
            "/education_levels",
            "/experiences",
            "/schedule",
            "/vacancy_branded_templates",
            "/vacancy_branded_templates/available",
            "/vacancy/addresses"
    );

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT);

    private final String baseUrl;
    private final String token;
    private final Path outputDir;
    private final List<String> endpoints;
    private Config(String baseUrl, String token, Path outputDir, List<String> endpoints) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.outputDir = outputDir;
        this.endpoints = endpoints;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String token() {
        return token;
    }

    public Path outputDir() {
        return outputDir;
    }

    public List<String> endpoints() {
        return endpoints;
    }

    static Config parse(String[] args) {
        String baseUrl = DEFAULT_BASE_URL;
        String token = DEFAULT_TOKEN;
        Path output = Path.of("output");
        boolean timestamped = false;
        List<String> endpoints = new ArrayList<>(DEFAULT_ENDPOINTS);

        for (String arg : args) {
            if (arg.startsWith("--token=")) {
                token = arg.substring("--token=".length());
            } else if (arg.startsWith("--output=")) {
                output = Path.of(arg.substring("--output=".length())).normalize();
            } else if (arg.startsWith("--base-url=")) {
                baseUrl = stripTrailingSlash(arg.substring("--base-url=".length()));
            } else if (arg.startsWith("--endpoints=")) {
                String value = arg.substring("--endpoints=".length());
                if (!value.isBlank()) {
                    endpoints = new ArrayList<>(normalizeEndpoints(Arrays.asList(value.split(","))));
                }
            } else if (arg.equals("--timestamped")) {
                timestamped = true;
            } else if (arg.equals("--help") || arg.equals("-h")) {
                throw new HelpRequestedException();
            } else if (!arg.isBlank()) {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }

        if (timestamped) {
            String suffix = STAMP.format(java.time.OffsetDateTime.now());
            output = output.resolve(suffix);
        }

        Set<String> unique = new LinkedHashSet<>(normalizeEndpoints(endpoints));
        if (unique.isEmpty()) {
            throw new IllegalArgumentException("Endpoint list must not be empty");
        }

        return new Config(baseUrl, token, output, List.copyOf(unique));
    }

    private static List<String> normalizeEndpoints(List<String> raw) {
        if (raw == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String item : raw) {
            if (item == null) {
                continue;
            }
            String trimmed = item.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            trimmed = trimmed.startsWith("/") ? trimmed : "/" + trimmed;
            trimmed = trimmed.replaceAll("//+", "/");
            result.add(trimmed);
        }
        return result;
    }

    private static String stripTrailingSlash(String value) {
        String trimmed = Objects.requireNonNull(value, "baseUrl").trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    static final class HelpRequestedException extends RuntimeException {
    }
}
