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

import org.exem.flamingo.shared.core.exception.ServiceException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Massive File Upload Controller.
 *
 * @author Byoung Gon, Kim
 * @author Hyo Kun, Park
 * @author Myeong ha, Kim
 * @since 0.1
 */
public class FileUploadHttpHandler implements HttpHandler {

    private static final Log LOG = LogFactory.getLog(FileUploadHttpHandler.class);

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        String response = null;
        try {
            InputStream is = httpExchange.getRequestBody();

            Map<String, Object> parameters = splitQuery(httpExchange.getRequestURI().getQuery());
            String fullyQualifiedPath = (String) parameters.get("fullyQualifiedPath");
            String username = (String) parameters.get("username");

            LOG.debug("[Flamingo] [Upload] Upload Path: " + fullyQualifiedPath);

            // 파일명을 포함하는 Path를 받아서 저장함. 그외 부가적인 처리는 별도로 하도록 함(파일 있는지 찾기).
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);

            if (fs.exists(new Path(fullyQualifiedPath))) {
                response = "FAILURE";
                httpExchange.sendResponseHeaders(500, response.length());
                LOG.warn("동일한 파일명이 존재합니다.");
                throw new ServiceException();
            }

            FSDataOutputStream os = fs.create(new Path(fullyQualifiedPath));

            FileCopyUtils.copy(is, os);

            // FIXME : 파일 업로드 후 왜 /를 찾아서 처리를 할까?
            if (fullyQualifiedPath.equalsIgnoreCase("/")) {
                response = "FAILURE";
                httpExchange.sendResponseHeaders(600, response.length());
                LOG.warn("루트(/)는 권한을 변경할 수 없습니다.");
                throw new ServiceException();
            }

            fs.setOwner(new Path(fullyQualifiedPath), username, username);

            os.flush();
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);

            response = "SUCCESS";
            httpExchange.sendResponseHeaders(200, response.length());
        } catch (Exception e) {
            LOG.warn("[Flamingo] [Upload] File Upload failed", e);

            response = "FAILURE";
            httpExchange.sendResponseHeaders(999, response.length());
        } finally {
            if (response != null) {
                outputStream.write(response.getBytes());
                IOUtils.closeQuietly(outputStream);
            } else {
                LOG.warn("[Flamingo] [Upload] Response is null");
            }
        }
    }

    private void parseGetParameters(HttpExchange exchange) throws UnsupportedEncodingException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        URI requestedUri = exchange.getRequestURI();
        String query = requestedUri.getRawQuery();
        parseQuery(query, parameters);
        exchange.setAttribute("parameters", parameters);
    }

    private Map<String, Object> parsePostParameters(HttpExchange exchange) throws IOException {
        if ("post".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, Object> parameters = new HashMap<>();
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
