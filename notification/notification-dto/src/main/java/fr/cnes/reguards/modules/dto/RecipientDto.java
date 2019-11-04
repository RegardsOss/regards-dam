/**
 *
 */
package fr.cnes.reguards.modules.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;

/**
 * @author kevin
 *
 */
public class RecipientDto {

    private Long id;

    @Valid
    private RuleDto rule;

    @NotNull
    private PluginConfiguration pluginConf;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RuleDto getRule() {
        return rule;
    }

    public void setRule(RuleDto rule) {
        this.rule = rule;
    }

    public PluginConfiguration getPluginConf() {
        return pluginConf;
    }

    public void setPluginConf(PluginConfiguration pluginConf) {
        this.pluginConf = pluginConf;
    }

    public static RecipientDto build(Long id, RuleDto rule, PluginConfiguration pluginConf) {
        RecipientDto recipient = new RecipientDto();
        recipient.setId(id);
        recipient.setPluginConf(pluginConf);
        recipient.setRule(rule);

        return recipient;
    }
}
