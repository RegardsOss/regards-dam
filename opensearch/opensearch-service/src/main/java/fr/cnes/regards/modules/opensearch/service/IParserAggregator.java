/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service;

/**
 * Aggregates an array of {@link IParser}s into a single one.
 * @author Xavier-Alexandre Brochard
 */
@FunctionalInterface
public interface IParserAggregator {

    /**
     * Composes the array of {@link IParser}s into a single one. The implementation will define the aggregation strategy
     * @param pParsers the array of parsers to compose
     * @return the composed parser
     */
    IParser aggregate(IParser... pParsers);

}
