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

import javax.validation.constraints.NotNull;

import org.dom4j.rule.Rule;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;

/**
 * Dto for a {@link Rule}
 * @author Kevin Marchois
 *
 */
public class RuleDto {

    private Long id;

    @NotNull
    private PluginConfiguration pluginConf;

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static RuleDto build(Long id, PluginConfiguration pluginConf, boolean enabled) {
        RuleDto rule = new RuleDto();
        rule.setId(id);
        rule.setPluginConf(pluginConf);
        rule.setEnabled(enabled);

        return rule;
    }
}
