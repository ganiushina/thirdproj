package ru.alta.hhdictdownloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the HH dictionary downloader.
 */
public final class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private App() {
    }

    public static void main(String[] args) {
        Config config;
        try {
            config = Config.parse(args);
        } catch (Config.HelpRequestedException e) {
            printUsage();
            return;
        } catch (Exception e) {
            LOGGER.error("Unable to parse arguments: {}", e.getMessage());
            LOGGER.debug("Argument parsing failed", e);
            printUsage();
            return;
        }

        try {
            new DictionaryDownloader(config).run();
        } catch (Exception e) {
            LOGGER.error("Download failed", e);
        }
    }

    private static void printUsage() {
        System.out.println("HH dictionary downloader\n" +
                "Usage: java -jar hh-dictionary-downloader.jar [options]\n" +
                "Options:\n" +
                "  --token=<value>        Override bearer token (default: 12345)\n" +
                "  --output=<dir>         Output directory for responses (default: ./output)\n" +
                "  --base-url=<url>       Override API base URL (default: https://api.hh.ru)\n" +
                "  --endpoints=a,b,c      Comma-separated list of endpoints to fetch\n" +
                "  --timestamped          Append timestamp folder inside output directory\n" +
                "  -h, --help             Show this message\n");
    }
}
