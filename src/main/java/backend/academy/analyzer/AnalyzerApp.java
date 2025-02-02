package backend.academy.analyzer;

import backend.academy.analyzer.args.CommandLineArgs;
import backend.academy.analyzer.parser.Log;
import backend.academy.analyzer.render.AbstractRenderer;
import backend.academy.analyzer.render.MarkdownRenderer;
import backend.academy.analyzer.statistics.LogAnalyzer;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;

/**
 * A console application that allows to collect statistics from files
 * containing NGINX logs in format:
 * <p>'$remote_addr - $remote_user [$time_local] ' '"$request" $status
 * $body_bytes_sent ' '"$http_referer" "$http_user_agent"'</p>
 * The result of the program is a string, containing formatted tables with statistics
 * in markdown or adoc format.
 */
@Log4j2
public class AnalyzerApp {

    private final BufferedWriter out;

    public AnalyzerApp(OutputStream outputStream) {
        out = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    /**
     * A method that allows you to collect statistics from NGINX logs in format:
     * <p>'$remote_addr - $remote_user [$time_local] ' '"$request" $status
     * $body_bytes_sent ' '"$http_referer" "$http_user_agent"'</p>
     * As input, the method receives an array of strings containing keys and values (command line arguments).
     * <p>Required keys:</p>
     * <p>{@code --path} - paths to one or more NGINX log files in the form of a local template or URL.
     * Note that you cannot pass more than one --path key.</p>
     * <p>Optional keys:</p>
     * <p>{@code --from} - date and time in ISO8601 format for analyzing records starting from the front time.</p>
     * <p>{@code --to} - date nd time in ISO8601 format for analyzing records up to and including the transmitted time.
     * </p>
     * <p>Note that you cannot pass more than one --from and --to key. In case of transfer of both keys, the logs in
     * the transmitted range will be analyzed.</p>
     * <p>{@code --format} - the format in which the statistics will be displayed. There are 2 formats available:
     * markdown and adoc (default - markdown). Note that you cannot pass more than one --format key.</p>
     * <p>{@code --filter-field} - The log field by which logs should be filtered before collecting statistics.</p>
     * <p>{@code --filter-value} - The log field's value by which logs should be filtered before collecting statistics.
     * </p>
     * <p>Note that --filter-field and --filter-value must go in a row. You can specify more than one pair of keys.</p>
     * <p>Names of fields, to filter on:</p>
     * <p>{@code remoteAddress}</p>
     * <p>{@code remoteUser}</p>
     * <p>{@code dateTime}</p>
     * <p>{@code httpRequest}</p>
     * <p>{@code httpStatus}</p>
     * <p>{@code bodyBytesSent}</p>
     * <p>{@code httpReferer}</p>
     * <p>{@code httpUserAgent}</p>
     *
     * @param args an array containing the above keys and values (command line arguments).
     */
    public void getStatistics(String... args) {
        try {
            CommandLineArgs cmdArgs = getCommandLineArgs(args);
            List<Path> localPaths = new ArrayList<>();
            List<URL> urlPaths = new ArrayList<>();
            processPaths(cmdArgs.paths(), localPaths, urlPaths);
            printStatistics(
                cmdArgs,
                localPaths,
                urlPaths,
                getFilterParams(cmdArgs),
                Objects.requireNonNullElse(cmdArgs.renderer(), new MarkdownRenderer())
            );
        } catch (ParameterException e) {
            log.error("Error: invalid arguments: {}", e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error occurred while writing to OutputStream: {}", e.getMessage(), e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                log.error("The error occurred when trying to close OutputStream: {}", e.getMessage(), e);
            }
        }
    }

    private static CommandLineArgs getCommandLineArgs(String[] args) {
        CommandLineArgs cmdArgs = new CommandLineArgs();
        JCommander jcommander = JCommander
            .newBuilder()
            .addObject(cmdArgs)
            .build();
        jcommander.parse(args);
        return cmdArgs;
    }

    private static void processPaths(List<String> paths, List<Path> localPaths, List<URL> urlPath) {
        for (var path : paths) {
            if (!getUrlIfValid(path, urlPath) && !getPathsToFilesIfPatternIsValid(path, localPaths)) {
                throw new ParameterException("Path \"" + path + "\" is invalid.");
            }
        }
    }

    @SuppressWarnings({"CatchParameterName", "IllegalIdentifierName"})
    private static boolean getUrlIfValid(String stringPath, List<URL> urlPaths) {
        try {
            return urlPaths.add(new URI(stringPath).toURL());
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException _) {
        }
        return false;
    }

    @SuppressWarnings({"CatchParameterName", "IllegalIdentifierName", "ParameterAssignment"})
    private static boolean getPathsToFilesIfPatternIsValid(String pathPattern, List<Path> localPaths) {
        try {
            Path pathTemplate = Paths.get(pathPattern);
            if (Files.isRegularFile(pathTemplate)) {
                localPaths.add(pathTemplate);
                return true;
            } else if (Files.isDirectory(pathTemplate)) {
                pathPattern += (File.separator + "*");
            } else {
                pathTemplate = processGlob(pathTemplate, pathPattern);
                if (pathTemplate == null) {
                    return false;
                }
            }
            PathMatcher matcher = FileSystems
                .getDefault()
                .getPathMatcher("glob:" + pathPattern);
            Files.walkFileTree(pathTemplate, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (matcher.matches(file)) {
                        localPaths.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (InvalidPathException | IOException _) {
        }
        return false;
    }

    private static Path processGlob(Path pathTemplate, String pathPattern) {
        String doubleStartPattern = File.separator + "**" + File.separator;
        if (pathPattern.contains(doubleStartPattern)) {
            return Paths.get(pathPattern.substring(0, pathPattern.indexOf(doubleStartPattern)));
        }
        return pathTemplate.getParent();
    }

    private static Map<String, String> getFilterParams(CommandLineArgs args) {
        List<String> fields = Objects.requireNonNullElse(args.filterFields(), List.of());
        List<String> values = Objects.requireNonNullElse(args.filterValues(), List.of());
        if (fields.size() != values.size()) {
            throw new ParameterException("Number filter fields doesn't correspond to number of filter values");
        }
        Map<String, String> filterFields = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            if (!Log.containsField(field)) {
                throw new ParameterException("Invalid filter filed. Log doesn't contain field: \"" + field + "\"");
            }
            filterFields.put(fields.get(i), values.get(i));
        }
        return filterFields;
    }

    private void printStatistics(
        CommandLineArgs args,
        List<Path> localPaths,
        List<URL> urlPaths,
        Map<String, String> filterParameters,
        AbstractRenderer renderer
    ) throws IOException {
        for (var path : localPaths) {
            out.write(renderer
                .render(LogAnalyzer.getStatisticsFromFile(path, args.from(), args.to(), filterParameters)));
            out.newLine();
        }
        for (var url : urlPaths) {
            out.write(renderer
                .render(LogAnalyzer.getStatisticsFromURL(url, args.from(), args.to(), filterParameters)));
            out.newLine();
        }
        out.flush();
    }
}
