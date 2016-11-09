/*
 * LICENSE_PLACEHOLDER
 */
/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
/*
 * Copyright 2015 Vlad Mihalcea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.cnes.regards.modules.entities.domain.json;

import java.util.Properties;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.usertype.DynamicParameterizedType;

/**
 * Descriptor for a Json type.
 *
 * @author Vlad MIhalcea
 *
 */
public class JsonBinaryType extends AbstractSingleColumnStandardBasicType<Object> implements DynamicParameterizedType {

    public JsonBinaryType() {
        super(JsonBinarySqlTypeDescriptor.INSTANCE, new JsonTypeDescriptor());
    }

    @Override
    public String getName() {
        return "jsonb";
    }

    @Override
    public void setParameterValues(Properties parameters) {
        ((JsonTypeDescriptor) getJavaTypeDescriptor()).setParameterValues(parameters);
    }

}