package backend.academy.analyzer.statistics;

import backend.academy.analyzer.error.InvalidLogFormatException;
import backend.academy.analyzer.parser.Log;
import backend.academy.analyzer.parser.LogParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * A class that allows to collect statistics from files
 * containing NGINX logs in format:
 * <p>'$remote_addr - $remote_user [$time_local] ' '"$request" $status
 * $body_bytes_sent ' '"$http_referer" "$http_user_agent"'</p>
 */
@UtilityClass
@Log4j2
public class LogAnalyzer {

    /**
     * A method that allows you to collect statistics from NGINX logs from local files in format:
     * <p>'$remote_addr - $remote_user [$time_local] ' '"$request" $status
     * $body_bytes_sent ' '"$http_referer" "$http_user_agent"'</p>
     *
     * @param path         path to the local resources, containing logs (local template paths).
     * @param from         date and time for analyzing records starting from the front time
     *                     (not including the transmitted time), null if the date and time
     *                     does not matter.
     * @param to           date and time for analyzing records up to (not including the transmitted time), null if the
     *                     date and time does not matter.
     * @param filterParams the parameters by which the values will be filtered
     *                     (the key is the name of the log field, the value is the value for filtering), empty map, if
     *                     there are no filter parameters.
     * @return LogStatistics objects, containing all collected information from sources with logs, if path is valid
     *     local path template and contains logs in the specified format, {@code null} otherwise.
     */
    public static LogStatistics getStatisticsFromFile(
        Path path,
        OffsetDateTime from,
        OffsetDateTime to,
        Map<String, String> filterParams
    ) {
        try (Stream<String> stream = Files.lines(path)) {
            LogStatistics statistics = new LogStatistics(from, to, path.toString());
            return getStatisticsFromStream(stream, statistics, filterParams);
        } catch (IOException e) {
            log.error("Error occurred while reading from file: \"{}\"", path, e);
        } catch (InvalidLogFormatException e) {
            log.error("Error: file \"{}\" contains logs in invalid format.", path, e);
        }
        return null;
    }

    /**
     * A method that allows you to collect statistics from NGINX logs from URL in format:
     * <p>'$remote_addr - $remote_user [$time_local] ' '"$request" $status
     * $body_bytes_sent ' '"$http_referer" "$http_user_agent"'</p>
     *
     * @param url          a string containing the URL leading to the resource, containing logs.
     * @param from         date and time for analyzing records starting from the front time
     *                     (not including the transmitted time), null if the date and time
     *                     does not matter.
     * @param to           date and time for analyzing records up to (not including the transmitted time), null if the
     *                     date and time does not matter.
     * @param filterParams the parameters by which the values will be filtered
     *                     (the key is the name of the log field, the value is the value for filtering), empty map, if
     *                     there are no filter parameters.
     * @return LogStatistics objects, containing all collected information from sources with logs, if path is valid
     *     URL and contains logs in the specified format, {@code null} otherwise.
     */
    public static LogStatistics getStatisticsFromURL(
        URL url,
        OffsetDateTime from,
        OffsetDateTime to,
        Map<String, String> filterParams
    ) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
             Stream<String> stream = reader.lines()) {
            LogStatistics statistics = new LogStatistics(from, to, url.toString());
            return getStatisticsFromStream(stream, statistics, filterParams);
        } catch (IOException e) {
            log.error("Error occurred while reading from URL: \"{}\".", url.toString(), e);
        } catch (InvalidLogFormatException e) {
            log.error("Error: URL \"{}\" contains logs in invalid format.", url.toString(), e);
        }
        return null;
    }

    private static LogStatistics getStatisticsFromStream(
        Stream<String> logsStream,
        LogStatistics statistics,
        Map<String, String> filterParams
    ) {
        logsStream
            .filter(log -> !log.isEmpty())
            .map(LogParser::parse)
            .filter(log -> matchesDates(log, statistics.fromDateTime(), statistics.toDateTime())
                && matchesFieldValue(log, filterParams))
            .forEach(statistics::update);
        return statistics;
    }

    private static boolean matchesDates(Log log, OffsetDateTime from, OffsetDateTime to) {
        OffsetDateTime date = OffsetDateTime.parse(log.dateTime());
        return (from == null || date.isAfter(from)) && (to == null || date.isBefore(to));
    }

    private static boolean matchesFieldValue(Log log, Map<String, String> filterParameters) {
        for (var filterParameter : filterParameters.entrySet()) {
            String fieldName = filterParameter.getKey();
            String field = Objects.requireNonNull(
                log.getFieldByName(fieldName),
                "log doesn't contain field \"" + fieldName + "\""
            );
            if (!field.startsWith(filterParameter.getValue())) {
                return false;
            }
        }
        return true;
    }
}
