/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.indexer.domain;

import java.util.Map;

/**
 * Simple search key where search and result type are the same
 * @param <T> search and result type
 * @author oroussel
 */
public class SimpleSearchKey<T> extends SearchKey<T, T> {

    public SimpleSearchKey(Map<String, Class<? extends T>> searchTypeMap, Class<T> resultClass) {
        super(searchTypeMap, resultClass);

    }

    public SimpleSearchKey(Map<String, Class<? extends T>> searchTypeMap) {
        super(searchTypeMap);

    }

    public SimpleSearchKey(String searchType, Class<? extends T> searchClass, Class<T> resultClass) {
        super(searchType, searchClass, resultClass);

    }

    public SimpleSearchKey(String searchType, Class<? extends T> searchClass) {
        super(searchType, searchClass);

    }

}
