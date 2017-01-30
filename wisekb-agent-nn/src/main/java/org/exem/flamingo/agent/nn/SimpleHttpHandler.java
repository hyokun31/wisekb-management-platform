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
package org.exem.flamingo.agent.nn;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Namenode Agent의 정상 동작 유무를 확인하는 HTTP 요청을 처리하는 핸들러.
 * 이 핸들러는 HTTP 요청을 받는 경우 동작 메시지를 클라이언트로 발송한다.
 * 이 메시지를 통해서 Namenode Agent의 동작을 확인한다.
 *
 * @author Byoung Gon, Kim
 * @since 0.1
 */
public class SimpleHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Headers headers = httpExchange.getResponseHeaders();
        headers.set("Content-Type", "text/html; charset=utf-8");
        httpExchange.sendResponseHeaders(200, 0);
        OutputStream outputStream = httpExchange.getResponseBody();
        String output = "I am listening for NameNode Agent...";
        outputStream.write(output.getBytes());
        outputStream.close();
    }

}
