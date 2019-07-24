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
package fr.cnes.regards.modules.indexer.domain.summary;

/**
 * A simple POJO that contains filesCount and filesSize properties
 * @author oroussel
 */
public class FilesSummary {

    protected long filesCount;

    protected long filesSize;

    public FilesSummary() {
    }

    public FilesSummary(long filesCount, long filesSize) {
        this.filesCount = filesCount;
        this.filesSize = filesSize;
    }

    public long getFilesCount() {
        return filesCount;
    }

    public void addFilesCount(long filesCount) {
        this.filesCount += filesCount;
    }

    public long getFilesSize() {
        return filesSize;
    }

    public void addFilesSize(long filesSize) {
        this.filesSize += filesSize;
    }

    @Override
    public String toString() {
        return "FilesSummary{" + "filesCount=" + filesCount + ", filesSize=" + filesSize + '}';
    }
}
