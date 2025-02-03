package analyzer.error;

/**
 * Thrown to indicate that the code has attempted to parse log with invalid format.
 * For example, the following code generates an {@code InvalidLogFormatException}:
 * <blockquote><pre>
 *     String invalidLogFormat = "invalid log format";
 *     Log log = LogParser.parse(invalidLogFormat);
 * </pre></blockquote>
 */
public class InvalidLogFormatException extends IllegalArgumentException {

    /**
     * Constructs an {@code InvalidLogFormatException} with the specified
     * detail message.
     *
     * @param message the detail message.
     */
    public InvalidLogFormatException(String message) {
        super(message);
    }
}
