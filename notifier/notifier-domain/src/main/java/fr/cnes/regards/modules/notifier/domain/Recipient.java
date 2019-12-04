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
    @JoinColumn(name = "rule_id", foreignKey = @ForeignKey(name = "fk_rule_id"))
    private Rule rule;

    @ManyToOne
    @NotNull(message = "Plugin id is required")
    @JoinColumn(name = "recipient_plugin_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_recipient_plugin_id"))
    private PluginConfiguration recipientPlugin;

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

    public PluginConfiguration getRecipientPlugin() {
        return recipientPlugin;
    }

    public void setRecipientPlugin(PluginConfiguration recipientPlugin) {
        this.recipientPlugin = recipientPlugin;
    }

    public static Recipient build(Rule rule, PluginConfiguration plugin) {
        Recipient recipient = new Recipient();
        recipient.setRule(rule);
        recipient.setRecipientPlugin(plugin);

        return recipient;
    }

}
