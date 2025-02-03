package analyzer.args;

import analyzer.render.AbstractRenderer;
import com.beust.jcommander.Parameter;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;

/**
 * A class containing command line arguments.
 */
@Getter
public class CommandLineArgs {
    @Parameter(
        names = {"--path"},
        required = true,
        variableArity = true,
        description = "Path to files with logs"
    )
    private List<String> paths;

    @Parameter(
        names = {"--from"},
        converter = OffsetDateTimeConverter.class,
        description = "The date from which the logs will be analyzed"
    )
    private OffsetDateTime from;

    @Parameter(
        names = {"--to"},
        converter = OffsetDateTimeConverter.class,
        description = "The date before which the logs will be analyzed"
    )
    private OffsetDateTime to;

    @Parameter(
        names = {"--format"},
        converter = RendererConverter.class,
        description = "Format of presentation of collected statistics"
    )
    private AbstractRenderer renderer;

    @Parameter(
        names = {"--filter-field"},
        variableArity = true,
        description = "The field to filter by"
    )
    private List<String> filterFields;

    @Parameter(
        names = {"--filter-value"},
        variableArity = true,
        description = "The value to filter by"
    )
    private List<String> filterValues;
}
