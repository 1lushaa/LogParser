package analyzer.render;

import analyzer.statistics.LogAnalyzer;
import analyzer.statistics.LogStatistics;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

abstract class AbstractRendererTest {

    private static final URI TEST_URI = URI.create(
        "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs"
    );

    protected abstract AbstractRenderer renderer();

    protected abstract String expectedOutput();

    private LogStatistics getStatistics() throws MalformedURLException {
        return LogAnalyzer.getStatisticsFromURL(
            TEST_URI.toURL(),
            OffsetDateTime.parse("2015-05-17T08:05:35Z"),
            OffsetDateTime.parse("2015-05-17T08:06:55Z"),
            new HashMap<>()
        );
    }

    @Test
    void validOutputTest_ExpectValidOutput() {
        LogStatistics statistics = Assertions.assertDoesNotThrow(this::getStatistics);
        assertThat(renderer().render(statistics)).isEqualTo(expectedOutput());
    }
}
