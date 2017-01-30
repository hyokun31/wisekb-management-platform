/*
 * Copyright 2012-2016 the Flamingo Community.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exem.flamingo.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ObjectMapperFactoryBeanTest {

    @Test
    public void getApplicationContext() throws JsonProcessingException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:org/exem/flamingo/shared/util/dummy-applicationContext.xml");
        ObjectMapper objectMapper = ctx.getBean("objectMapper", ObjectMapper.class);

        Map map = new HashMap();
        map.put("name", "helloworld");
        String jsonString = objectMapper.writeValueAsString(map);

        Assert.assertEquals(1, StringUtils.countOccurrencesOf(jsonString, "helloworld"));
        Assert.assertEquals(1, StringUtils.countOccurrencesOf(jsonString, "name"));
    }
}
