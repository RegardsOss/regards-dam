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
package fr.cnes.regards.modules.dam.service.models;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.dam.domain.models.attributes.Fragment;

/**
 * Fragment service interface
 *
 * @author Marc Sordi
 *
 */
public interface IFragmentService {

    List<Fragment> getFragments();

    Fragment addFragment(Fragment pFragment) throws ModuleException;

    Fragment getFragment(Long pFragmentId) throws ModuleException;

    Fragment updateFragment(Long pFragmentId, Fragment pFragment) throws ModuleException;

    void deleteFragment(Long pFragmentId) throws ModuleException;

    void exportFragment(Long pFragmentId, OutputStream pOutputStream) throws ModuleException;

    Fragment importFragment(InputStream pInputStream) throws ModuleException;
}
