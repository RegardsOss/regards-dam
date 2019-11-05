/**
 *
 */
package fr.cnes.reguards.modules.notifier.dto;

import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.reguards.modules.dto.type.NotificationType;

/**
 * @author kevin
 *
 */
public class RuleDto {

    private Long id;

    @NotNull
    private PluginConfiguration pluginConf;

    @NotNull
    private NotificationType type;

    @NotNull
    private boolean enabled = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PluginConfiguration getPluginConf() {
        return pluginConf;
    }

    public void setPluginConf(PluginConfiguration pluginConf) {
        this.pluginConf = pluginConf;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static RuleDto build(Long id, PluginConfiguration pluginConf, boolean enabled, NotificationType type) {
        RuleDto rule = new RuleDto();
        rule.setId(id);
        rule.setPluginConf(pluginConf);
        rule.setEnabled(enabled);
        rule.setType(type);

        return rule;
    }
}
