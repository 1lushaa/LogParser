package backend.academy.analyzer.statistics;

import backend.academy.analyzer.parser.Log;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import org.apache.commons.math3.util.Pair;

/**
 * A class representing statistics compiled based on various logs.
 */
public final class LogStatistics {
    @Getter
    private BigInteger numberOfRequests = BigInteger.ZERO;

    private final Map<String, BigInteger> requestsToResources = new HashMap<>();
    private final Map<String, BigInteger> responsesCodes = new HashMap<>();
    private final Map<String, BigInteger> remoteAddresses = new HashMap<>();
    private final Map<String, BigInteger> httpReferrers = new HashMap<>();

    @Getter
    private final OffsetDateTime fromDateTime;
    @Getter
    private final OffsetDateTime toDateTime;
    @Getter
    private final String pathToFile;

    private final List<BigInteger> serverResponsesSizes = new ArrayList<>();

    public LogStatistics(OffsetDateTime from, OffsetDateTime to, String file) {
        fromDateTime = from;
        toDateTime = to;
        pathToFile = file;
    }

    /**
     * A method that returns top 3 most frequently requested resources and number of their requests.
     *
     * @return the list contains pairs of the form (resource_name, number_of_requests),
     *     sorted in descending order of the number of requests.
     */
    public List<Pair<String, BigInteger>> theMostFrequentlyRequestedResources() {
        return getSortedList(requestsToResources);
    }

    /**
     * A method that returns top 3 most common response codes and their numbers.
     *
     * @return the list contains pairs of the form (response_code, quantity),
     *     sorted in descending order of the quantity.
     */
    public List<Pair<String, BigInteger>> theMostCommonResponseCodes() {
        return getSortedList(responsesCodes);
    }

    /**
     * Calculates the average size of the server response in bytes.
     *
     * @return the average size of the server response in bytes.
     */
    public BigInteger averageServerResponseSize() {
        if (Objects.equals(numberOfRequests, BigInteger.ZERO)) {
            return BigInteger.ZERO;
        }
        return serverResponsesSizes
            .stream()
            .reduce(BigInteger.ZERO, BigInteger::add)
            .divide(numberOfRequests);
    }

    /**
     * Calculates 95% percentile of the server response size.
     *
     * @return 95% percentile of the server response size.
     */
    @SuppressWarnings("MagicNumber")
    public BigInteger responseSizePercentile() {
        int elementsToSkipNum = serverResponsesSizes.size() / 100 * 95;
        elementsToSkipNum += elementsToSkipNum > 0 ? elementsToSkipNum - 1 : 0;
        return serverResponsesSizes
            .stream()
            .sorted()
            .skip(elementsToSkipNum)
            .findFirst()
            .orElse(BigInteger.ZERO);
    }

    /**
     * A method that returns top 3 most common remote addresses and their numbers.
     *
     * @return the list contains pairs of the form (remote_address, quantity),
     *     sorted in descending order of the quantity.
     */
    public List<Pair<String, BigInteger>> theMostFrequentRemoteAddresses() {
        return getSortedList(remoteAddresses);
    }

    /**
     * A method that returns top 3 most common HTTP referrers and their numbers.
     *
     * @return the list contains pairs of the form (remote_address, quantity),
     *     sorted in descending order of the quantity.
     */
    public List<Pair<String, BigInteger>> theMostFrequentHttpReferer() {
        return getSortedList(httpReferrers);
    }

    @SuppressWarnings("MagicNumber")
    private static List<Pair<String, BigInteger>> getSortedList(Map<String, BigInteger> map) {
        return map.entrySet()
            .stream()
            .sorted((firstEntry, secondEntry) -> secondEntry.getValue().compareTo(firstEntry.getValue()))
            .limit(3)
            .map(entry -> Pair.create(entry.getKey(), entry.getValue()))
            .toList();
    }

    /**
     * Updates the data based on the transmitted log.
     *
     * @param log the log to be taken into account in the statistics.
     */
    public void update(Log log) {
        numberOfRequests = numberOfRequests.add(BigInteger.ONE);
        requestsToResources.put(
            log.getHttpRequestBody(),
            requestsToResources.getOrDefault(log.getHttpRequestBody(), BigInteger.ZERO).add(BigInteger.ONE)
        );
        responsesCodes.put(
            log.httpStatus(),
            responsesCodes.getOrDefault(log.httpStatus(), BigInteger.ZERO).add(BigInteger.ONE)
        );
        remoteAddresses.put(
            log.remoteAddress(),
            remoteAddresses.getOrDefault(log.remoteAddress(), BigInteger.ZERO).add(BigInteger.ONE)
        );
        httpReferrers.put(
            log.httpReferer(),
            httpReferrers.getOrDefault(log.httpReferer(), BigInteger.ZERO).add(BigInteger.ONE)
        );
        serverResponsesSizes.add(new BigInteger(log.bodyBytesSent()));
    }
}
