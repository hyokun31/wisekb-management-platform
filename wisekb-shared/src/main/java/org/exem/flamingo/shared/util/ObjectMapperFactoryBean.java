/**
 * Copyright (C) 2011 Flamingo Project (http://www.cloudine.io).
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.exem.flamingo.shared.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class ObjectMapperFactoryBean implements FactoryBean<ObjectMapper>, InitializingBean {

    private ObjectMapper objectMapper;

    private boolean isIndentOutput = false;

    @Override
    public ObjectMapper getObject() throws Exception {
        return this.objectMapper;
    }

    @Override
    public Class<ObjectMapper> getObjectType() {
        return ObjectMapper.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.objectMapper = new ObjectMapper();

        if (isIndentOutput) {
            this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        }
    }

    public void setIndentOutput(boolean isIndentOutput) {
        this.isIndentOutput = isIndentOutput;
    }
}
