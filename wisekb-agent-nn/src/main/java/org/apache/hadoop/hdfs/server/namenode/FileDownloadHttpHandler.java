/*
 * Copyright (C) 2012-2016 the Flamingo Community.
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
package org.apache.hadoop.hdfs.server.namenode;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Massive File Download Controller.
 *
 * @author Byoung Gon, Kim
 * @author Hyo Kun, Park
 * @since 0.1
 */
public class FileDownloadHttpHandler implements HttpHandler {

    private static final Log LOG = LogFactory.getLog(FileDownloadHttpHandler.class);

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        String response = null;

        try {
            Map<String, Object> parameters = splitQuery(httpExchange.getRequestURI().getQuery().toString());
            String fullyQualifiedPath = (String) parameters.get("fullyQualifiedPath");

            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);
            FSDataInputStream is = fs.open(new Path(fullyQualifiedPath));
            FileStatus fileStatus = fs.getFileStatus(new Path(fullyQualifiedPath));
            OutputStream os = httpExchange.getResponseBody(); // OutputStream

            httpExchange.sendResponseHeaders(200, fileStatus.getLen());

            FileCopyUtils.copy(is, os);

            IOUtils.closeQuietly(is);

            os.flush();
            IOUtils.closeQuietly(os);

            response = "SUCCESS";
        } catch (IOException e) {
            LOG.warn("[Flamingo] [Download] File Download failed", e);

            response = "FAILURE";
            httpExchange.sendResponseHeaders(999, response.length());
        } finally {
            if (response != null) {
                outputStream.write(response.getBytes());
                IOUtils.closeQuietly(outputStream);
            } else {
                LOG.warn("[Flamingo] [Download] Response is null");
            }
        }
    }

    private Map<String, Object> parsePostParameters(HttpExchange exchange) throws IOException {
        if ("post".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, Object> parameters = (Map<String, Object>) exchange.getAttribute("parameters"); // TODO 여기 주의
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine();
            parseQuery(query, parameters);
            return parameters;
        }
        return null;
    }

    public static Map<String, Object> splitQuery(String url) throws UnsupportedEncodingException {
        Map<String, Object> query_pairs = new HashMap<String, Object>();
        String query = url;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

    private void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {
        if (query != null) {
            String pairs[] = query.split("[&]");

            for (String pair : pairs) {
                String param[] = pair.split("[=]");

                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);
                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<String>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }
}
