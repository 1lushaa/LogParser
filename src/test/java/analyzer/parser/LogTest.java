package analyzer.parser;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Log test.")
class LogTest {

    private static Stream<String> getValidFields() {
        return Stream.of(
            "remoteAddress",
            "remoteUser",
            "dateTime",
            "httpRequest",
            "httpStatus",
            "bodyBytesSent",
            "httpReferer",
            "httpUserAgent"
        );
    }

    private static Stream<String> getInvalidFields() {
        return Stream.of(
            "remoteAddressss",
            "remoteUserssww",
            "dateTimfwfwe"
        );
    }

    private static Stream<Arguments> getLogsWithHttpBody() {
        return Stream.of(
            Arguments.of(
                new Log("", "", "", "GET /downloads/product_1 HTTP/1.1", "", "", "", ""),
                "/downloads/product_1"
            ),
            Arguments.of(
                new Log("", "", "", "GET /downloads/product_1 HTTP/1.1", "", "", "", ""),
                "/downloads/product_1"
            ),
            Arguments.of(
                new Log("", "", "", "GET /downloads/product_3 HTTP/1.1", "", "", "", ""),
                "/downloads/product_3"
            ),
            Arguments.of(
                new Log("", "", "", "POST /downloads/product_2 HTTP/1.1", "", "", "", ""),
                "/downloads/product_2"
            )
        );
    }

    private static Stream<Arguments> getLogsWithField() {
        return Stream.of(
            Arguments.of(new Log("address", "", "", "", "", "", "", ""), "remoteAddress", "address"),
            Arguments.of(new Log("", "user", "", "", "", "", "", ""), "remoteUser", "user"),
            Arguments.of(new Log("", "", "time", "", "", "", "", ""), "dateTime", "time"),
            Arguments.of(new Log("", "", "", "request", "", "", "", ""), "httpRequest", "request"),
            Arguments.of(new Log("", "", "", "", "status", "", "", ""), "httpStatus", "status"),
            Arguments.of(new Log("", "", "", "", "", "bytes", "", ""), "bodyBytesSent", "bytes"),
            Arguments.of(new Log("", "", "", "", "", "", "referer", ""), "httpReferer", "referer"),
            Arguments.of(new Log("", "", "", "", "", "", "", "agent"), "httpUserAgent", "agent")
        );
    }

    @ParameterizedTest
    @MethodSource("getValidFields")
    @DisplayName("Contains valid field test.")
    public void containsValidFieldTest_ExpectTrue(String field) {
        assertThat(Log.containsField(field)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("getInvalidFields")
    @DisplayName("Doesn't contain invalid field.")
    public void containsInvalidFieldTest_ExpectFalse(String field) {
        assertThat(Log.containsField(field)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("getLogsWithHttpBody")
    @DisplayName("Getting http body test.")
    public void getHttpBodyTest_ExpectValidBody(Log log, String body) {
        assertThat(log.getHttpRequestBody()).isEqualTo(body);
    }

    @ParameterizedTest
    @MethodSource("getLogsWithField")
    @DisplayName("Get valid fields by name test.")
    public void getFieldByValidNameTest_ExpectValidField(Log log, String fieldName, String expected) {
        assertThat(log.getFieldByName(fieldName)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Get fields by invalid names test.")
    public void getInvalidFieldByNameTest_ExpectNull() {
        Log testLog = new Log("", "", "", "", "", "", "", "");
        assertThat(testLog.getFieldByName("invalid name")).isNull();
    }
}
