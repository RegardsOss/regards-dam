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
package fr.cnes.regards.modules.models.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * Model service
 *
 * @author Marc Sordi
 *
 */
public interface IModelService {

    List<Model> getModels(EntityType pType);

    Model createModel(Model pModel) throws ModuleException;

    Model getModel(Long pModelId) throws ModuleException;

    Model getModelByName(String pModelName) throws ModuleException;

    Model updateModel(Long pModelId, Model pModel) throws ModuleException;

    void deleteModel(Long pModelId) throws ModuleException;

    Model duplicateModel(Long pModelId, Model pModel) throws ModuleException;

    void exportModel(Long pModelId, OutputStream pOutputStream) throws ModuleException;

    Model importModel(InputStream pInputStream) throws ModuleException;
}
