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
package fr.cnes.regards.modules.model.dto.properties;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author sbinda
 *
 */
public class MarkdownURL {

    private static Pattern pattern = Pattern.compile("\\[(.*)\\]\\((.*)\\)");

    private String label;

    private URL url;

    public MarkdownURL(String label, URL url) {
        super();
        this.label = label;
        this.url = url;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public String toString() {
        if ((label == null) && (url == null)) {
            return null;
        }
        if (label == null) {
            return url.toString();
        }
        return String.format("[%s](%s)", label, url.toString());
    }

    public static MarkdownURL build(String stringValue) throws MalformedURLException {
        Matcher m = pattern.matcher(stringValue);
        if (m.find()) {
            return new MarkdownURL(m.group(1), new URL(m.group(2)));
        } else {
            return new MarkdownURL(null, new URL(stringValue));
        }
    }

}
