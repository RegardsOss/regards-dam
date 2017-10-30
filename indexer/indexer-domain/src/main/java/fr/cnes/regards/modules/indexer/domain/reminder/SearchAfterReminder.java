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
package fr.cnes.regards.modules.indexer.domain.reminder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import fr.cnes.regards.framework.jpa.json.GsonUtil;
import fr.cnes.regards.modules.indexer.domain.SearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

/**
 * @author oroussel
 */
public class SearchAfterReminder extends Reminder {

    private static final String TYPE = "SEARCH_AFTER";

    private String searchAfterSortValues;

    private int nextOffset;

    private int nextPageSize;

    public SearchAfterReminder(ICriterion crit, SearchKey<?, ?> searchKey, Sort sort, Pageable nextPage) {
        super();
        // Generate a unique String with concatenation of gson objects and join keys
        String unique = GsonUtil.toString(crit) + "__" + GsonUtil.toString(nextPage) + "__" + GsonUtil
                .toString(searchKey.getSearchIndex()) + "__" + GsonUtil.toString(searchKey.getSearchTypes()) + "__"
                + GsonUtil.toString(sort);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) { // Does not occur
            throw new RuntimeException(e); // NOSONAR
        }
        md.reset();
        byte[] buffer = unique.getBytes();
        md.update(buffer);
        byte[] digest = md.digest();

        // docId is equivalent to a unique hashcode
        this.docId = "";
        for (int i = 0; i < digest.length; i++) {
            this.docId += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
        }
        this.nextOffset = nextPage.getOffset();
        this.nextPageSize = nextPage.getPageSize();
    }

    public Object[] getSearchAfterSortValues() {
        return GsonUtil.fromString(searchAfterSortValues, Object[].class);
    }

    public void setSearchAfterSortValues(Object[] searchAfterSortValues) {
        this.searchAfterSortValues = GsonUtil.toString(searchAfterSortValues);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
