/**
 *
 */
package fr.cnes.regards.modules.notification.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import fr.cnes.reguards.modules.dto.type.NotificationType;

/**
 * @author kevin
 *
 */
@Entity
@Table(name = "t_rule")
public class Rule {

    @Id
    @SequenceGenerator(name = "ruleSequence", initialValue = 1, sequenceName = "seq_rule")
    @GeneratedValue(generator = "ruleSequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private NotificationType type;

    @ManyToOne
    @NotNull(message = "Plugin configuration is required")
    @JoinColumn(name = "plugin_configuration_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rule_plugin_configuration_id"))
    private PluginConfiguration pluginCondConfiguration;

    @Column(name = "enable", nullable = false)
    private boolean enable = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public PluginConfiguration getPluginCondConfiguration() {
        return pluginCondConfiguration;
    }

    public void setPluginCondConfiguration(PluginConfiguration pluginCondConfiguration) {
        this.pluginCondConfiguration = pluginCondConfiguration;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public static Rule builder(NotificationType type, PluginConfiguration plugin) {
        Rule rule = new Rule();
        rule.setPluginCondConfiguration(plugin);
        rule.setType(type);

        return rule;
    }
}
