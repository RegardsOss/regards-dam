package fr.cnes.regards.modules.models.domain;

import java.util.Set;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * POJO allowing us to map which plugin configurations and plugin metadata can be mapped to which attribute type
 *
 * @author Sylvain VISSIERE-GUERINET
 */
public class TypeMetadataConfMapping {

    private AttributeType attrType;

    private Set<PluginConfiguration> pluginConfigurations;

    private Set<PluginMetaData> pluginMetaDatas;

    public TypeMetadataConfMapping() {
    }

    public TypeMetadataConfMapping(AttributeType attrType, Set<PluginConfiguration> pluginConfigurations,
            Set<PluginMetaData> pluginMetaData) {
        this.attrType = attrType;
        this.pluginConfigurations = pluginConfigurations;
        this.pluginMetaDatas = pluginMetaData;
    }

    public AttributeType getAttrType() {
        return attrType;
    }

    public void setAttrType(AttributeType attrType) {
        this.attrType = attrType;
    }

    public Set<PluginConfiguration> getPluginConfigurations() {
        return pluginConfigurations;
    }

    public void setPluginConfigurations(Set<PluginConfiguration> pluginConfigurations) {
        this.pluginConfigurations = pluginConfigurations;
    }

    public Set<PluginMetaData> getPluginMetaDatas() {
        return pluginMetaDatas;
    }

    public void setPluginMetaDatas(Set<PluginMetaData> pluginMetaDatas) {
        this.pluginMetaDatas = pluginMetaDatas;
    }
}
