/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.domain;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 *
 * This class offer a method to write Json from a list of POJO and a method to build a list of POJO with a Json.
 *
 * @author cmertz
 * @since 1.0-SNAPSHOT
 */
public final class ColumnMapper {

    private final static Logger LOGGER = LoggerFactory.getLogger(ColumnMapper.class);

    private static ObjectMapper mapper_ = new ObjectMapper();

    /**
     *
     * Constructeur
     *
     * @since 1.0-SNAPSHOT
     */
    private ColumnMapper() {
        // Static class
    }

    /**
     * Write a Json from a variable number of Column
     *
     * @param pCol
     * @return Json
     */
    public static String toJson(Column... pCol) {
        String result = "";
        try {
            result = mapper_.writeValueAsString(pCol);
        }
        catch (final JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
            result = "";
        }
        return result;
    }

    /**
     * Trasform a Json in a list of Column
     *
     * @param jsonSource
     * @return the list of column
     */
    public static List<Column> json2List(String jsonSource) {
        List<Column> result = null;
        try {
            result = mapper_.readValue(jsonSource, new TypeReference<List<Column>>() {
            });
        }
        catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

}
