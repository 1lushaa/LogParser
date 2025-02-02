package backend.academy.analyzer.statistics;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LogAnalyzer test.")
class LogAnalyzerTest {

    private static final Path TEST_DIRECTORY_PATH = Paths.get("analyzer_test_dir");
    private static final Path TEST_FILE_PATH = TEST_DIRECTORY_PATH.resolve("analyzer_test.txt");
    private static final URI TEST_URI = URI.create(
        "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs"
    );

    private static Stream<Path> getSourcesPaths() {
        return Stream.of(TEST_FILE_PATH);
    }

    private static Stream<URL> getSourcesURL() throws MalformedURLException {
        return Stream.of(TEST_URI.toURL());
    }

    private static void assertStatisticsMatchesExpectedValues(
        LogStatistics statistics,
        BigInteger numberOfRequests,
        BigInteger averageResponseSize,
        BigInteger percentile,
        List<Pair<String, BigInteger>> theMostCommonResponseCodes,
        List<Pair<String, BigInteger>> theMostFrequentlyRequestedResources,
        List<Pair<String, BigInteger>> theMostFrequentHttpReferer,
        List<Pair<String, BigInteger>> theMostFrequentHttpAddress
    ) {
        assertThat(statistics.numberOfRequests()).isEqualByComparingTo(numberOfRequests);
        assertThat(statistics.averageServerResponseSize()).isEqualByComparingTo(averageResponseSize);
        assertThat(statistics.responseSizePercentile()).isEqualByComparingTo(percentile);
        assertThat(statistics.theMostCommonResponseCodes())
            .containsExactlyInAnyOrderElementsOf(theMostCommonResponseCodes);
        assertThat(statistics.theMostFrequentlyRequestedResources())
            .containsExactlyInAnyOrderElementsOf(theMostFrequentlyRequestedResources);
        assertThat(statistics.theMostFrequentHttpReferer())
            .containsExactlyInAnyOrderElementsOf(theMostFrequentHttpReferer);
        assertThat(statistics.theMostFrequentRemoteAddresses())
            .containsExactlyInAnyOrderElementsOf(theMostFrequentHttpAddress);
    }

    @BeforeAll
    public static void prepareNewDirectoryWithTestFiles() throws IOException {
        Files.createDirectory(TEST_DIRECTORY_PATH);
        Files.createFile(TEST_FILE_PATH);
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(TEST_URI.toURL().openStream().readAllBytes())))) {
            for (int i = 0; i < 10; i++) {
                String log = reader.readLine() + System.lineSeparator();
                Files.write(TEST_FILE_PATH, log.getBytes(), StandardOpenOption.APPEND);
            }
        }
    }

    @AfterAll
    public static void deleteTestDirectoryAndFiles() throws IOException {
        Files.delete(TEST_FILE_PATH);
        Files.delete(TEST_DIRECTORY_PATH);
    }

    @Test
    @DisplayName("Non-existent path test.")
    public void nonexistentPathTest_ExpectNull() {
        LogStatistics statistics =
            LogAnalyzer.getStatisticsFromFile(Paths.get("nonexistent_source.txt"), null, null, Map.of());
        assertThat(statistics).isNull();
    }

    @ParameterizedTest
    @MethodSource("getSourcesURL")
    @DisplayName("Successful getting statistics from URL sources.")
    public void gettingStatisticsFromUrlTest_ExpectNonEmptyStatistics(URL url) {
        LogStatistics statistics = LogAnalyzer.getStatisticsFromURL(url, null, null, Map.of());
        assertThat(statistics).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("getSourcesPaths")
    @DisplayName("Successful getting statistics from file sources.")
    public void gettingStatisticsFromFileTest_ExpectNonEmptyStatistics(Path path) {
        LogStatistics statistics = LogAnalyzer.getStatisticsFromFile(path, null, null, Map.of());
        assertThat(statistics).isNotNull();
    }

    @Test
    @DisplayName("Valid statistics with FROM date parameter test.")
    public void gettingValidStatisticsWithFromDateTimeTest_ExpectValidStatistics() {
        LogStatistics statistics = LogAnalyzer.getStatisticsFromFile(
            TEST_FILE_PATH,
            OffsetDateTime.parse("2015-05-17T08:05:35Z"),
            null,
            Map.of()
        );
        assertStatisticsMatchesExpectedValues(
            statistics,
            BigInteger.TWO,
            BigInteger.valueOf(166),
            BigInteger.ZERO,
            List.of(Pair.create("304", BigInteger.ONE), Pair.create("404", BigInteger.ONE)),
            List.of(Pair.create("/downloads/product_1", BigInteger.TWO)),
            List.of(Pair.create("-", BigInteger.TWO)),
            List.of(Pair.create("93.180.71.3", BigInteger.ONE), Pair.create("217.168.17.5", BigInteger.ONE))
        );
    }

    @Test
    @DisplayName("Valid statistics with TO date parameter test.")
    public void gettingValidStatisticsWithToDateTimeTest_ExpectValidStatistics() {
        LogStatistics statistics = LogAnalyzer.getStatisticsFromFile(
            TEST_FILE_PATH,
            null,
            OffsetDateTime.parse("2015-05-17T08:05:03Z"),
            Map.of()
        );
        assertStatisticsMatchesExpectedValues(
            statistics,
            BigInteger.TWO,
            BigInteger.valueOf(168),
            BigInteger.ZERO,
            List.of(Pair.create("304", BigInteger.ONE), Pair.create("404", BigInteger.ONE)),
            List.of(
                Pair.create("/downloads/product_1", BigInteger.ONE),
                Pair.create("/downloads/product_2", BigInteger.ONE)
            ),
            List.of(Pair.create("-", BigInteger.TWO)),
            List.of(Pair.create("217.168.17.5", BigInteger.ONE), Pair.create("80.91.33.133", BigInteger.ONE))
        );
    }

    @Test
    @DisplayName("Valid statistics with fields filter parameters test.")
    public void gettingValidStatisticsWithFieldsFiltersTest_ExpectValidStatistics() {
        LogStatistics statistics = LogAnalyzer.getStatisticsFromFile(
            TEST_FILE_PATH,
            null,
            null,
            Map.of("remoteAddress", "217.168.17.5")
        );
        assertStatisticsMatchesExpectedValues(
            statistics,
            BigInteger.valueOf(4),
            BigInteger.valueOf(412),
            BigInteger.valueOf(332),
            List.of(Pair.create("200", BigInteger.TWO), Pair.create("404", BigInteger.TWO)),
            List.of(
                Pair.create("/downloads/product_1", BigInteger.TWO),
                Pair.create("/downloads/product_2", BigInteger.TWO)
            ),
            List.of(Pair.create("-", BigInteger.valueOf(4))),
            List.of(Pair.create("217.168.17.5", BigInteger.valueOf(4)))
        );
    }
}
