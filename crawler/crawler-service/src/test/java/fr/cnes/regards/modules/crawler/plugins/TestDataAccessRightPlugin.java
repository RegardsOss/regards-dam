package fr.cnes.regards.modules.crawler.plugins;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.plugins.IDataObjectAccessFilter;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

@Plugin(id = "TestDataAccessRightPlugin", version = "4.0.0-SNAPSHOT", description = "test", author = "REGARDS Team",
        contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI", url = "https://github.com/RegardsOss")
public class TestDataAccessRightPlugin implements IDataObjectAccessFilter {

    public static final String LABEL_PARAM = "label";

    @PluginParameter(label = LABEL_PARAM)
    private String label;

    @Override
    public ICriterion getSearchFilter() {
        return ICriterion.eq("feature.label", this.label);
    }

}
