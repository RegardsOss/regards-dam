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
package fr.cnes.regards.modules.indexer.domain;

import java.util.Map;

public class JoinEntitySearchKey<S, R> extends SearchKey<S, R> {

    public JoinEntitySearchKey(Map<String, Class<? extends S>> searchTypeMap, Class<R> resultClass) {
        super(searchTypeMap, resultClass);

    }

    public JoinEntitySearchKey(Map<String, Class<? extends S>> searchTypeMap) {
        super(searchTypeMap);

    }

    public JoinEntitySearchKey(String searchType, Class<? extends S> searchClass, Class<R> resultClass) {
        super(searchType, searchClass, resultClass);

    }

    public JoinEntitySearchKey(String searchType, Class<? extends S> searchClass) {
        super(searchType, searchClass);

    }

}
