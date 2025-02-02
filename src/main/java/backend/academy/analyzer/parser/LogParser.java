package backend.academy.analyzer.parser;

import backend.academy.analyzer.error.InvalidLogFormatException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

/**
 * A class that allows to parse NGINX-logs in format:
 * <p>'$remote_addr - $remote_user [$time_local]' '"$request" $status $body_bytes_sent
 * ' '"$http_referer" "$http_user_agent"'</p>
 */
@UtilityClass
public final class LogParser {

    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

    private static final String IPV4_REGEX = "(((|[1-9]|1\\d|2[0-4])\\d|25[0-5])\\.?\\b){4}";
    private static final String IPV6_REGEX = "((^|:)([0-9a-fA-F]{0,4})){1,8}";
    private static final String HTTP_REQUEST_REGEX =
        "(GET|POST|PUT|DELETE|HEAD|OPTIONS|PATCH) (/[^ ]*) HTTP/(1\\.[01]|2\\.0)";
    private static final String HTTP_RESPONSE_CODE_REGEX = "[1-5]\\d{2}";
    private static final String DATE_TIME_REGEX = "\\d{2}/[A-Za-z]{3}/\\d{4}:\\d{2}:\\d{2}:\\d{2} [+-]\\d{4}";

    private static final Pattern LOG_PATTERN = Pattern.compile(
        "(?<remoteAddress>" + IPV4_REGEX + "|" + IPV6_REGEX + ") "
            + "- "
            + "(?<remoteUser>\\S*) "
            + "\\[(?<dateTime>" + DATE_TIME_REGEX + ")] "
            + "\"(?<httpRequest>" + HTTP_REQUEST_REGEX + ")\" "
            + "(?<httpStatus>" + HTTP_RESPONSE_CODE_REGEX + ") "
            + "(?<bodyBytesSent>\\d+) "
            + "\"(?<httpReferer>[^\"]*)\" "
            + "\"(?<httpUserAgent>[^\"]+)\""
    );

    /**
     * Returns Log object that contains data from string representation of given log.
     *
     * @param log the log, the Log object representation of which is expected.
     * @return a Log object, contains data from {@code log}.
     * @throws InvalidLogFormatException if {@code log} isn't matches format:
     *                                   <p>'$remote_addr - $remote_user [$time_local]' '"$request"
     *                                   $status $body_bytes_sent ' '"$http_referer" "$http_user_agent"'</p>
     */
    public static Log parse(String log) {
        Matcher logMatcher = LOG_PATTERN.matcher(log);
        if (!logMatcher.matches()) {
            throw new InvalidLogFormatException("Attempt to parse log in invalid format.");
        }
        return new Log(
            logMatcher.group("remoteAddress"),
            logMatcher.group("remoteUser"),
            getISODateTime(logMatcher.group("dateTime")),
            logMatcher.group("httpRequest"),
            logMatcher.group("httpStatus"),
            logMatcher.group("bodyBytesSent"),
            logMatcher.group("httpReferer"),
            logMatcher.group("httpUserAgent")
        );
    }

    private static String getISODateTime(String dateTime) {
        return OffsetDateTime.parse(dateTime, INPUT_DATE_FORMATTER).toString();
    }
}
