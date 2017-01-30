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

import java.lang.management.ManagementFactory;

/**
 * JMX를 기반으로 현재 JVM의 PID를 알아내는 등의 다양한 시스템 기능을 제공하는 유틸리티.
 *
 * @author Byoung Gon, Kim
 * @since 0.1
 */
public class SystemUtils {

    public static String getPid() {
        try {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            if (name != null) {
                return name.split("@")[0];
            }
        } catch (Throwable ex) {
            // Ignore
        }
        return "????";
    }

}
