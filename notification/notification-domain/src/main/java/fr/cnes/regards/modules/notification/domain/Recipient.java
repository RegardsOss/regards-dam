/**
 *
 */
package fr.cnes.regards.modules.notification.domain;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;

/**
 * @author kevin
 *
 */
@Entity
@Table(name = "t_recipient")
public class Recipient {

    @Id
    @SequenceGenerator(name = "recipientSequence", initialValue = 1, sequenceName = "seq_recipient")
    @GeneratedValue(generator = "recipientSequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @NotNull(message = "A rule is required")
    @JoinColumn(name = "rule_id", foreignKey = @ForeignKey(name = "fk_rule_id"), nullable = false)
    private Rule rule;

    @ManyToOne
    @NotNull(message = "Plugin configuration is required")
    @JoinColumn(name = "plugin_configuration_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rule_plugin_configuration_id"))
    private PluginConfiguration pluginCondConfiguration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public PluginConfiguration getPluginCondConfiguration() {
        return pluginCondConfiguration;
    }

    public void setPluginCondConfiguration(PluginConfiguration pluginCondConfiguration) {
        this.pluginCondConfiguration = pluginCondConfiguration;
    }

    public static Recipient builder(Rule rule, PluginConfiguration plugin) {
        Recipient recipient = new Recipient();
        recipient.setRule(rule);
        recipient.setPluginCondConfiguration(plugin);

        return recipient;
    }

}
