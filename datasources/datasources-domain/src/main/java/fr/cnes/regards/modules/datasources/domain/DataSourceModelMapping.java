/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.datasources.domain;

import java.util.List;

import com.google.gson.annotations.JsonAdapter;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * This class is used to map a data source to a {@link Model}
 * 
 * @author Christophe Mertz
 *
 */
@JsonAdapter(value = ModelMappingAdapter.class)
public class DataSourceModelMapping {

    /**
     * The {@link Model} identifier
     */
    private Long model;

    /**
     * The mapping between the attribute of the {@link Model} of the attributes of th data source
     */
    private List<AbstractAttributeMapping> attributesMapping;

    public DataSourceModelMapping() {
        super();
    }

    public DataSourceModelMapping(Long modelId, List<AbstractAttributeMapping> attributeMappings) {
        super();
        this.model = modelId;
        this.attributesMapping = attributeMappings;
    }

    public Long getModel() {
        return model;
    }

    public void setModel(Long modelId) {
        this.model = modelId;
    }

    public List<AbstractAttributeMapping> getAttributesMapping() {
        return attributesMapping;
    }

    public void setAttributesMapping(List<AbstractAttributeMapping> attributeMappings) {
        this.attributesMapping = attributeMappings;
    }

}
