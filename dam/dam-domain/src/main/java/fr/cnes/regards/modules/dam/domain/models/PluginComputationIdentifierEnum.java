/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.domain.models;

/**
 * Enumeration to easily identify computation plugins.
 *
 * @author Sébastien Binda
 *
 */
public enum PluginComputationIdentifierEnum {

    COUNT(PluginComputationIdentifierEnum.COUNT_VALUE),
    MAX_DATE(PluginComputationIdentifierEnum.MAX_DATE_VALUE),
    MIN_DATE(PluginComputationIdentifierEnum.MIN_DATE_VALUE),
    LONG_SUM_COUNT(PluginComputationIdentifierEnum.LONG_SUM_COUNT_VALUE),
    INT_SUM_COUNT(PluginComputationIdentifierEnum.INT_SUM_COUNT_VALUE);

    public final static String COUNT_VALUE = "CountPlugin";

    public final static String INT_SUM_COUNT_VALUE = "IntSumComputePlugin";

    public final static String LONG_SUM_COUNT_VALUE = "LongSumComputePlugin";

    public final static String MAX_DATE_VALUE = "MaxDateComputePlugin";

    public final static String MIN_DATE_VALUE = "MinDateComputePlugin";

    private final String value;

    PluginComputationIdentifierEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    /**
     * Parse a Computation plugin type by is id.
     * @param pluginId
     * @return {@link PluginComputationIdentifierEnum}
     */
    public static PluginComputationIdentifierEnum parse(String pluginId) {
        if (COUNT_VALUE.equals(pluginId)) {
            return COUNT;
        }
        if (INT_SUM_COUNT_VALUE.equals(pluginId)) {
            return INT_SUM_COUNT;
        }
        if (LONG_SUM_COUNT_VALUE.equals(pluginId)) {
            return LONG_SUM_COUNT;
        }
        if (MAX_DATE_VALUE.equals(pluginId)) {
            return MAX_DATE;
        }
        if (MIN_DATE_VALUE.equals(pluginId)) {
            return MIN_DATE;
        }
        return null;
    }

}
