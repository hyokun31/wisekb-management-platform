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
package wisekb.web.oozie.workflowdesigner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface OozieWorkflowService {

    String makeShellActionXml(Map param) throws IOException;

    String localOozieJobSend(String xmlString);

    List<Map> getWorkflows();

    Map getRecentWorkflow();

    void saveWorkflow(Map param);

    void updateWorkflow(Map param);

    void deleteWorkflow(long id);
}
