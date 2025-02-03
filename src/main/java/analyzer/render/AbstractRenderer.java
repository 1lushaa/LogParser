package analyzer.render;

import analyzer.statistics.LogStatistics;
import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.math3.util.Pair;

/**
 * An abstract class that allows to get a string formatted
 * representation of the statistics.
 */
@SuppressWarnings("MultipleStringLiterals")
public abstract class AbstractRenderer {
    /**
     * Returns string formatted representation of the given statistics
     *
     * @param statistics statistics, the string formatted representation of which is expected.
     * @return a string containing a formatted representation of the given statistics.
     */
    public String render(LogStatistics statistics) {
        if (statistics == null) {
            return "";
        }
        return getGeneralInformation(statistics)
            + getRequestedResources(statistics)
            + getResponsesCodes(statistics)
            + getRemoteAddresses(statistics)
            + getHttpReferer(statistics);
    }

    protected abstract String getFormatedHeader(String name);

    private String getGeneralInformation(LogStatistics statistics) {
        Map<String, String> generalInformation = new LinkedHashMap<>();
        generalInformation.put("File(-s)", "`" + statistics.pathToFile() + "`");
        generalInformation.put(
            "Starting date",
            Objects.requireNonNullElse(statistics.fromDateTime(), '-').toString()
        );
        generalInformation.put(
            "Ending date",
            Objects.requireNonNullElse(statistics.toDateTime(), '-').toString()
        );
        generalInformation.put("Number of requests", statistics.numberOfRequests().toString());
        generalInformation.put("AverageResponseSize", statistics.averageServerResponseSize().toString());
        generalInformation.put("95p response's size's", statistics.responseSizePercentile().toString());
        return getTable(
            generalInformation,
            "General information",
            "Metric",
            "Value"
        );
    }

    private String getRequestedResources(LogStatistics statistics) {
        return getTable(
            getLinkedHashMap(statistics.theMostFrequentlyRequestedResources()),
            "Requested resources",
            "Resource",
            "Requests"
        );
    }

    private String getResponsesCodes(LogStatistics statistics) {
        return getTable(
            getLinkedHashMap(statistics.theMostCommonResponseCodes()),
            "Responses codes",
            "Code",
            "Count"
        );
    }

    private String getRemoteAddresses(LogStatistics statistics) {
        return getTable(
            getLinkedHashMap(statistics.theMostFrequentRemoteAddresses()),
            "Remote addresses",
            "Address",
            "Count"
        );
    }

    private String getHttpReferer(LogStatistics statistics) {
        return getTable(
            getLinkedHashMap(statistics.theMostFrequentHttpReferer()),
            "Http referers",
            "Referer",
            "Count"
        );
    }

    private String getTable(
        Map<String, String> metrics,
        String statisticsName,
        String firstColumnName,
        String secondColumnName
    ) {
        int metricsFieldsWidth = Math.max(getMaxColumnSize(metrics.keySet()), firstColumnName.length() + 2);
        int metricsValuesWidth = Math.max(getMaxColumnSize(metrics.values()), secondColumnName.length() + 2);
        StringBuilder table = new StringBuilder();
        table.append(getFormatedHeader(statisticsName)).append(System.lineSeparator().repeat(2));
        getTableHead(table, metricsFieldsWidth, metricsValuesWidth, firstColumnName, secondColumnName);
        getTableBody(table, metricsFieldsWidth, metricsValuesWidth, metrics);
        table.append(System.lineSeparator());
        return table.toString();
    }

    private static void getTableHead(
        StringBuilder table,
        int metricsFieldsWidth,
        int metricsValuesWidth,
        String firstTitle,
        String secondTitle
    ) {
        getRow(table, metricsFieldsWidth, metricsValuesWidth, firstTitle, secondTitle);
        table
            .append("|:")
            .append("-".repeat(metricsFieldsWidth - 2))
            .append(":|:")
            .append("-".repeat(metricsValuesWidth - 2))
            .append(":|")
            .append(System.lineSeparator());
    }

    private static void getTableBody(
        StringBuilder table,
        int metricsFieldsWidth,
        int metricsValuesWidth,
        Map<String, String> metrics
    ) {
        for (var pair : metrics.entrySet()) {
            getRow(table, metricsFieldsWidth, metricsValuesWidth, pair.getKey(), pair.getValue());
        }
    }

    private static void getRow(
        StringBuilder table,
        int metricsFieldsWidth,
        int metricsValuesWidth,
        String field,
        String value
    ) {
        table
            .append("| ")
            .append(field)
            .append(" ".repeat(metricsFieldsWidth - 1 - field.length()))
            .append("| ")
            .append(value)
            .append(" ".repeat(metricsValuesWidth - 1 - value.length()))
            .append("|")
            .append(System.lineSeparator());
    }

    private static int getMaxColumnSize(Collection<String> columnFields) {
        return columnFields
            .stream()
            .map(String::length)
            .max(Integer::compareTo)
            .orElse(0) + 2;
    }

    private static Map<String, String> getLinkedHashMap(List<Pair<String, BigInteger>> list) {
        Map<String, String> map = new LinkedHashMap<>();
        for (var pair : list) {
            map.put("`" + pair.getKey() + "`", pair.getValue().toString());
        }
        return map;
    }
}
