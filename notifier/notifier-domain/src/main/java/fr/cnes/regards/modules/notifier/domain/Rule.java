/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.notifier.domain;

import java.util.Set;

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
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.OneToMany;
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
@NamedEntityGraphs({ @NamedEntityGraph(name = "Rule.recipients", attributeNodes = @NamedAttributeNode("recipients")),
        @NamedEntityGraph(name = "Rule.pluginCondConfiguration",
                attributeNodes = @NamedAttributeNode("pluginCondConfiguration")) })
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

    @OneToMany(mappedBy = "rule")
    private Set<Recipient> recipients;

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

    public Set<Recipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(Set<Recipient> recipients) {
        this.recipients = recipients;
    }

    public static Rule build(Long id, PluginConfiguration pluginConf, boolean enabled, NotificationType type) {
        Rule rule = new Rule();
        rule.setPluginCondConfiguration(pluginConf);
        rule.setType(type);
        rule.setEnable(enabled);
        rule.setId(id);
        return rule;
    }
}
