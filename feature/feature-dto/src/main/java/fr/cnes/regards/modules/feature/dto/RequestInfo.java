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
package fr.cnes.regards.modules.feature.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;

/**
 * REST feature request response information
 *
 * @author Marc SORDI
 *
 * @param <ID> String or {@link FeatureUniformResourceName} according to the context
 */
public class RequestInfo<ID> {

    /**
     * Mapping between feature id or URN and request id
     */
    private Map<ID, String> granted = new HashMap<>();

    /**
     * Mapping between SIP id and denied reason
     */
    private Multimap<ID, String> denied = ArrayListMultimap.create();

    private List<String> messages;

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public void addGrantedRequest(ID id, String requestId) {
        granted.put(id, requestId);
    }

    public void addDeniedRequest(ID id, String reason) {
        denied.put(id, reason);
    }

    public void addDeniedRequest(ID id, Iterable<String> reasons) {
        denied.putAll(id, reasons);
    }

    public Map<ID, String> getGranted() {
        return granted;
    }

    public Multimap<ID, String> getDenied() {
        return denied;
    }

    public void setGranted(Map<ID, String> granted) {
        this.granted = granted;
    }

    public void setDenied(Multimap<ID, String> denied) {
        this.denied = denied;
    }
}
