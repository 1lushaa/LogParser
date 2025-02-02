package backend.academy.analyzer.parser;

import java.util.Map;
import java.util.function.Function;

/**
 * A class representing an object model of the NGINX log.
 */
public record Log(
    String remoteAddress,
    String remoteUser,
    String dateTime,
    String httpRequest,
    String httpStatus,
    String bodyBytesSent,
    String httpReferer,
    String httpUserAgent) {

    private static final Map<String, Function<Log, String>> GETTERS_BY_NAME_MAPPER = Map.of(
        "remoteAddress", Log::remoteAddress,
        "remoteUser", Log::remoteUser,
        "dateTime", Log::dateTime,
        "httpRequest", Log::httpRequest,
        "httpStatus", Log::httpStatus,
        "bodyBytesSent", Log::bodyBytesSent,
        "httpReferer", Log::httpReferer,
        "httpUserAgent", Log::httpUserAgent
    );

    /**
     * Checks whether NGINX log contains specified field.
     *
     * @param field name of the field.
     * @return {@code true} if NGINX log contains specified {@code field}, {@code false} otherwise.
     */
    public static boolean containsField(String field) {
        return GETTERS_BY_NAME_MAPPER.containsKey(field);
    }

    /**
     * The method that returns the body of the HTTP request contained in the log.
     *
     * @return the body of the HTTP request contained in the log.
     */
    public String getHttpRequestBody() {
        return httpRequest.split(" ")[1];
    }

    /**
     * Accepts the name of the field and returns its value if such a field exists, null otherwise.
     * @param fieldName the name of the field whose value is expected.
     * @return {@code fieldName}'s value if such field exists, null otherwise.
     */
    public String getFieldByName(String fieldName) {
        if (!GETTERS_BY_NAME_MAPPER.containsKey(fieldName)) {
            return null;
        }
        return GETTERS_BY_NAME_MAPPER.get(fieldName).apply(this);
    }
}
