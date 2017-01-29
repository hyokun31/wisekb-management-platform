/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package wisekb.flume.sink.jdbc;

import org.apache.flume.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.remove;
import static org.apache.commons.lang.StringUtils.substring;

public class MessageParser implements Parser {

    private static final Logger LOG = LoggerFactory.getLogger(MessageParser.class);

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    @Override
    public Map parse(Event event) {
        HashMap parsed = new HashMap();
        try {
            byte[] body = event.getBody();
            String bodyString = new String(body);
            String[] columns = bodyString.split("\\|");

            String removedDate = remove(columns[0], ":");

            parsed.put("year", substring(removedDate, 0, 4));
            parsed.put("month", substring(removedDate, 4, 6));
            parsed.put("day", substring(removedDate, 6, 8));
            parsed.put("hh", substring(removedDate, 8, 10));
            parsed.put("mm", substring(removedDate, 10, 12));
            parsed.put("ss", substring(removedDate, 12, 14));

            parsed.put("date", removedDate);
            parsed.put("level", columns[1]);
            parsed.put("annotatorType", columns[2].split(":")[1]);
            parsed.put("processType", columns[3].split(":")[1]);
            parsed.put("casIndex", columns[4].split(":")[1]);
            parsed.put("data", columns[5].split(":")[1]);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Message Format", e);
        }
        return parsed;
    }
}
