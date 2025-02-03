package analyzer.parser;

import analyzer.error.InvalidLogFormatException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

class LogParserTest {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

    private static final Random RANDOM_GENERATOR = new SecureRandom();

    private static final Set<String> IP_ADDRESS = Set.of(
        "255.255.255.255",
        "1111:1111:1111:1111:1111:1111:1111:1111",
        "192.168.10.150",
        "174.222.216.130",
        "4.62.109.168",
        "2f1a:a80b:22d5:312b:1ca5:33aa:9000:3dcc",
        "b9f6:f889:9657:b395:5c00:6980:c31b:8650"
    );
    private static final Set<String> REMOTE_USER = Set.of(
        "remote_user",
        "-",
        "abnb2d2d23",
        "test"
    );
    private static final Set<String> DATE_TIME = Set.of(
        "[17/May/2015:08:05:24 +0000]",
        "[01/Feb/2000:12:35:23 +0300]"
    );
    private static final Set<String> BODY_BYTES_SENT = Set.of(
        "10000000000000000000",
        "900548990875312004309903445130",
        "4394632479328432596345435454354354"
    );
    private static final Set<String> HTTP_REFERER = Set.of(
        "\"http_referer\"",
        "\"qwerty\"",
        "\"-\""
    );
    private static final Set<String> HTTP_USER_AGENT = Set.of(
        "\"http_user_agent\"",
        "\"anfjkbwel\"",
        "\"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.16)\""
    );
    private static final Set<String> HTTP_VERSION = Set.of(
        "HTTP/1.0",
        "HTTP/1.1",
        "HTTP/2.0"
    );
    private static final Set<String> HTTP_REQUEST_METHOD = Set.of(
        "GET",
        "POST",
        "PUT",
        "DELETE",
        "HEAD",
        "OPTIONS",
        "PATCH"
    );
    private static final Set<String> HTTP_REQUEST_BODY = Set.of(
        "/downloads/product_2",
        "/downloads/product_1",
        "/file.html"
    );
    private static final Set<String> HTTP_RESPONSE_CODE = IntStream.range(100, 600).
        mapToObj(Integer::toString).
        collect(Collectors.toSet());

    private static Stream<String> getInvalidRemoteAddressLogs() {
        String validRemainingPart = " - " + getRandomElement(REMOTE_USER) + " " + getRandomElement(DATE_TIME)
            + " " + getRandomHttpRequest() + " " + getRandomElement(HTTP_RESPONSE_CODE) + " "
            + getRandomElement(BODY_BYTES_SENT) + " " + getRandomElement(HTTP_REFERER) + " "
            + getRandomElement(HTTP_USER_AGENT);
        return Stream.of(
            "256.168.0.1" + validRemainingPart,
            "255.256.0.250" + validRemainingPart,
            "255.168.900.240" + validRemainingPart,
            "255.168.1.1000" + validRemainingPart,
            "200G:cdba:0000:0000:0000:0000:3257:9652" + validRemainingPart,
            "2003:cdbH:0000:0000:0000:0000:3257:9652" + validRemainingPart,
            "2003:cdb3:000J:0000:0000:0000:3257:9652" + validRemainingPart,
            "2003:cdb3:0000:0K00:0000:0000:3257:9652" + validRemainingPart,
            "2003:cdb3:0000:0000:00L0:0000:3257:9652" + validRemainingPart,
            "2003:cdb3:0000:0000:0000:00z0:3257:9652" + validRemainingPart,
            "2003:cdb3:0000:0000:0000:0070:32579:9652" + validRemainingPart,
            "2003:cdb3:0000:0000:0000:0070:3257:96528" + validRemainingPart
        );
    }

    private static Stream<String> getInvalidDateTimeLogs() {
        String validFirstPart = getRandomElement(IP_ADDRESS) + " - " + getRandomElement(REMOTE_USER) + " ";
        String validSecondPart = " " + getRandomHttpRequest() + " " + getRandomElement(HTTP_RESPONSE_CODE) + " "
            + getRandomElement(BODY_BYTES_SENT) + " " + getRandomElement(HTTP_REFERER) + " "
            + getRandomElement(HTTP_USER_AGENT);
        return Stream.of(
            validFirstPart + "17/May/2015:08:05:24 +0000" + validSecondPart,
            validFirstPart + "(17/May/2015:08:05:24 +0000)" + validSecondPart,
            validFirstPart + "[17.May.2015:08:05:24 +0000]" + validSecondPart,
            validFirstPart + "[2022-09-27 18:00:00.000]" + validSecondPart,
            validFirstPart + "[17/May/2015:08:05:24]" + validSecondPart,
            validFirstPart + "[2015/May/17:08:05:24 +0000]" + validSecondPart,
            validFirstPart + "[2015/05/17:08:05:24 +0000]" + validSecondPart
        );
    }

    private static Stream<String> getInvalidHttpRequestLogs() {
        String validFirstPart = getRandomElement(IP_ADDRESS) + " - " + getRandomElement(REMOTE_USER)
            + " " + getRandomElement(DATE_TIME) + " ";
        String validSecondPart = " " + getRandomElement(HTTP_RESPONSE_CODE) + " "
            + getRandomElement(BODY_BYTES_SENT) + " " + getRandomElement(HTTP_REFERER) + " "
            + getRandomElement(HTTP_USER_AGENT);
        return Stream.of(
            validFirstPart + getRandomHttpRequest().replace("\"", ""),
            validFirstPart + "\"GOT /downloads/product_1 HTTP/1.1\"" + validSecondPart,
            validFirstPart + "\"POST /downloads/ product_1 HTTP/1.1\"" + validSecondPart,
            validFirstPart + "\"POST /downloads/product_1 HTTP/0.8\"" + validSecondPart
        );
    }

    private static Stream<Arguments> getValidLogs() {
        List<Arguments> validLogs = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String dateTime = getRandomElement(DATE_TIME)
                .replace("[", "")
                .replace("]", "");
            Log logObject = new Log(
                getRandomElement(IP_ADDRESS),
                getRandomElement(REMOTE_USER),
                OffsetDateTime.parse(dateTime, DATE_FORMATTER).toString(),
                getRandomHttpRequest().replace("\"", ""),
                getRandomElement(HTTP_RESPONSE_CODE),
                getRandomElement(BODY_BYTES_SENT),
                getRandomElement(HTTP_REFERER).replace("\"", ""),
                getRandomElement(HTTP_USER_AGENT).replace("\"", "")
            );
            String logString =
                logObject.remoteAddress() + " - " + logObject.remoteUser() + " [" + dateTime + "] " + "\"" +
                    logObject.httpRequest() + "\" " + logObject.httpStatus() + " " + logObject.bodyBytesSent() + " \"" +
                    logObject.httpReferer() + "\" \"" + logObject.httpUserAgent() + "\"";
            validLogs.add(Arguments.of(logObject, logString));
        }
        return validLogs.stream();
    }

    private static String getRandomHttpRequest() {
        return "\"" + getRandomElement(HTTP_REQUEST_METHOD) + " " + getRandomElement(HTTP_REQUEST_BODY)
            + " " + getRandomElement(HTTP_VERSION) + "\"";
    }

    private static <T> T getRandomElement(Set<T> set) {
        return set.stream()
            .skip(RANDOM_GENERATOR.nextInt(set.size()))
            .findFirst()
            .orElseThrow();
    }

    @ParameterizedTest
    @MethodSource("getInvalidRemoteAddressLogs")
    @DisplayName("Invalid remote address test.")
    public void invalidRemoteAddressTest_ExpectException(String log) {
        Assertions.assertThrows(InvalidLogFormatException.class, () -> LogParser.parse(log));
    }

    @ParameterizedTest
    @MethodSource("getInvalidDateTimeLogs")
    @DisplayName("Invalid date time test.")
    public void invalidDateTimeTest_ExpectException(String log) {
        Assertions.assertThrows(InvalidLogFormatException.class, () -> LogParser.parse(log));
    }

    @ParameterizedTest
    @MethodSource("getInvalidHttpRequestLogs")
    @DisplayName("Invalid HTTP request test.")
    public void invalidHttpRequestTest_ExpectException(String log) {
        Assertions.assertThrows(InvalidLogFormatException.class, () -> LogParser.parse(log));
    }

    @ParameterizedTest
    @MethodSource("getValidLogs")
    @DisplayName("Valid logs test.")
    public void validLogTest_ExpectValidParsedLog(Log logObject, String logString) {
        Log resultedLog = Assertions.assertDoesNotThrow(() -> LogParser.parse(logString));
        assertThat(resultedLog).isEqualTo(logObject);
    }
}
