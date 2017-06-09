/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

/**
 *
 * List all available static properties
 *
 * @author Marc Sordi
 *
 */
public final class StaticProperties {

    private StaticProperties() {
    }

    // ##########-AbstractEntity-##########

    // URN
    public static final String IP_ID = "ipId";

    // String
    public static final String LABEL = "label";

    // Model
    public static final String MODEL_TYPE = "model";

    // String
    public static final String MODEL_NAME = MODEL_TYPE + ".name";

    // String
    public static final String MODEL_DESCRIPTION = MODEL_TYPE + ".description";

    // String
    public static final String MODEL_VERSION = MODEL_TYPE + ".version";

    // Date
    public static final String LAST_UPDATE = "lastUpdate";

    // Date
    public static final String CREATION_DATE = "creationDate";

    // Long
    public static final String ID = "id";

    // String
    public static final String SIP_ID = "sipId";

    // String list
    public static final String TAGS = "tags";

    // String list
    public static final String GROUPS = "groups";

    // Geometry
    public static final String GEOMETRY = "geometry";

    // ##########-AbstractDataEntity-##########

    // List of DataFile
    public static final String FILES = "files";

    // ##########-AbstractDescEntity-##########

    // DescriptionFile
    public static final String DESCRIPTION_FILE = "descriptionFile";

    // ##########-Collection-##########

    // ##########-DataObject-##########

    public static final String DATASOURCE_ID = "dataSourceId";

    // Long list
    public static final String DATASET_MODEL_IDS = "datasetModelIds";

    // ##########-Dataset-##########

    // int
    public static final String SCORE = "score";

    // Long
    public static final String DATA_MODEL = "dataModel";

    // String list
    public static final String QUOTATIONS = "quotations";

    // String
    public static final String LICENCE = "licence";

    // ##########-Document-##########

    // ##########-From GSON factory EntityAdapterFactory-##########

    public static final String ENTITY_TYPE = "entityType";

}
