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
package fr.cnes.reguards.modules.notifier.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;

/**
 * Dto for a {@link Recipient}
 * @author Kevin Marchois
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
