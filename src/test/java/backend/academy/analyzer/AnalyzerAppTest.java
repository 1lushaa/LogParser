package backend.academy.analyzer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@DisplayName("AnalyzerApp test.")
class AnalyzerAppTest {

    private static final Path TEST_FILES_ROOT_DIR = Paths.get("analyzer_test_dir");

    private static final List<Path> TEST_FILES = List.of(
        TEST_FILES_ROOT_DIR.resolve("analyzerTestFile_1.txt"),
        TEST_FILES_ROOT_DIR.resolve("analyzerTestFile_2.txt")
    );

    private static final URI TEST_URI = URI.create(
        "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs"
    );

    private static final String VALID_DATE = "2015-05-17T08:05:32Z";
    private static final String INVALID_DATE = "17/May/2015T08:05:35Z";

    private AnalyzerApp app;
    private OutputStream output;

    private static Stream<List<String>> getValidPaths() {
        return Stream.concat(
            TEST_FILES
                .stream()
                .map(path -> List.of("--path", path.toString())),
            Stream.of(
                List.of("--path", TEST_URI.toString()),
                List.of("--path", TEST_FILES_ROOT_DIR + File.separator + "*.txt")
            )
        );
    }

    private static Stream<List<String>> getValidLogFieldFilter() {
        String path = TEST_FILES.getFirst().toString();
        return Stream.of(
            List.of(
                "--path", path,
                "--filter-field", "remoteAddress",
                "--filter-value", "93.180.71.3"),
            List.of(
                "--path", path,
                "--filter-field", "remoteUser",
                "--filter-value", "-"),
            List.of(
                "--path", path,
                "--filter-field", "dateTime",
                "--filter-value", VALID_DATE),
            List.of(
                "--path", path,
                "--filter-field", "httpRequest",
                "--filter-value", "GET /downloads/product_1 HTTP/1.1"),
            List.of(
                "--path", path,
                "--filter-field", "httpStatus",
                "--filter-value", "304"),
            List.of(
                "--path", path,
                "--filter-field", "bodyBytesSent",
                "--filter-value", "0"),
            List.of(
                "--path", path,
                "--filter-field", "httpReferer",
                "--filter-value", "-"),
            List.of(
                "--path", path,
                "--filter-field", "httpUserAgent",
                "--filter-value", "Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)")
        );
    }

    private void assertDoesntThrowAndEmptyOutput(String[] args) {
        Assertions.assertDoesNotThrow(() -> app.getStatistics(args));
        assertThat(output.toString()).isEmpty();
    }

    private void assertDoesntThrowAndNotEmptyOutput(List<String> args) {
        app.getStatistics(args.toArray(new String[] {}));
        assertThat(output.toString()).isNotEmpty();
    }

    @BeforeAll
    public static void prepareTestDirectoriesWithFiles() throws IOException {
        Files.createDirectory(TEST_FILES_ROOT_DIR);
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(TEST_URI.toURL().openStream().readAllBytes())))) {
            StringBuilder logs = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                logs.append(reader.readLine()).append(System.lineSeparator());
            }
            for (var file : TEST_FILES) {
                Files.createFile(file);
                Files.write(file, logs.toString().getBytes());
            }
        }
    }

    @AfterAll
    public static void deleteTestDirectoriesWithFiles() throws IOException {
        for (var file : TEST_FILES) {
            Files.delete(file);
        }
        Files.delete(TEST_FILES_ROOT_DIR);
    }

    @BeforeEach
    void prepareTest() {
        output = new ByteArrayOutputStream();
        app = new AnalyzerApp(output);
    }

    @ParameterizedTest
    @MethodSource("getValidPaths")
    @DisplayName("Valid paths test.")
    public void validFilePathTest_ExpectNotEmptyOutput(List<String> args) {
        assertDoesntThrowAndNotEmptyOutput(args);
    }

    @Test
    @DisplayName("Invalid paths test.")
    public void invalidPathsTest_ExpectNoException() {
        assertDoesntThrowAndEmptyOutput(new String[] {"--path", "invalid_path"});
    }

    @Test
    @DisplayName("Valid from date test.")
    public void validFromDateTest_ExpectNotEmptyOutput() {
        assertDoesntThrowAndNotEmptyOutput(List.of(
            "--path", TEST_FILES_ROOT_DIR + File.separator + "*.txt",
            "--from", VALID_DATE
        ));
    }

    @Test
    @DisplayName("Invalid from date test.")
    public void invalidFromDateTest_ExpectNoException() {
        assertDoesntThrowAndEmptyOutput(new String[] {
            "--path", TEST_FILES_ROOT_DIR + File.separator + "*.txt",
            "--from", INVALID_DATE
        });
    }

    @Test
    @DisplayName("Valid to date test.")
    public void validToDateTest_ExpectNotEmptyOutput() {
        assertDoesntThrowAndNotEmptyOutput(List.of(
            "--path", TEST_FILES_ROOT_DIR + File.separator + "*.txt",
            "--to", VALID_DATE
        ));
    }

    @Test
    @DisplayName("Invalid to date test.")
    public void invalidToDateTest_ExpectNoException() {
        assertDoesntThrowAndEmptyOutput(new String[] {
            "--path", TEST_FILES_ROOT_DIR + File.separator + "*.txt",
            "--to", INVALID_DATE
        });
    }

    @Test
    @DisplayName("Valid render format test.")
    public void validRenderFormatTest_ExpectNotEmptyOutput() {
        assertDoesntThrowAndNotEmptyOutput(List.of(
            "--path", TEST_FILES_ROOT_DIR + File.separator + "*.txt",
            "--format", "adoc"
        ));
    }

    @Test
    @DisplayName("Invalid render format test.")
    public void invalidRenderFormatTest_ExpectNoException() {
        assertDoesntThrowAndEmptyOutput(new String[] {
            "--path", TEST_FILES_ROOT_DIR + File.separator + "*.txt",
            "--format", "unknown_format"
        });
    }

    @ParameterizedTest
    @MethodSource("getValidLogFieldFilter")
    @DisplayName("Valid log field filter test.")
    public void validLogFieldFilterTestTest_ExpectNotEmptyOutput(List<String> args) {
        assertDoesntThrowAndNotEmptyOutput(args);
    }

    @Test
    @DisplayName("Invalid log field filter test.")
    public void invalidLogFieldFilterTest_ExpectNoException() {
        assertDoesntThrowAndEmptyOutput(new String[] {
            "--path", TEST_FILES_ROOT_DIR + File.separator + "*.txt",
            "--filter-field", "unknown_field",
            "--filter-value", "-"
        });
    }
}
