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
package wisekb.web.hadoop.resourcemanager;

import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import wisekb.shared.core.exception.ServiceException;
import wisekb.shared.core.exception.WholeBodyException;
import wisekb.shared.core.rest.Response;
import wisekb.shared.util.ApplicationContextRegistry;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping(value = "/hadoop/resourcemanager")
public class ResourceManagerController {

    /**
     * SLF4J Logging
     */
    private Logger logger = LoggerFactory.getLogger(ResourceManagerController.class);

    @Autowired
    ResourceManagerService service;

    @RequestMapping(value = "/clustermetrics", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response clusterMetrics(HttpSession httpSession) {
        Response response = new Response();

        try {
            response.setMap(service.getClusterMetrics());
            return response;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @RequestMapping(value = "/clustermetrics/last", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response getLastClusterMetrics(HttpSession httpSession) {
        Response response = new Response();

        try {
            List<Map> metricsList = service.getLastClusterMetrics();

            for (Map metric : metricsList) {
                response.getMap().put(metric.get("metric_name").toString(), metric.get("metrics"));
            }

            return response;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @RequestMapping(value = "/configuration", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response configuration() {

        try {
            Response response = new Response();
            response.setSuccess(true);
            SortedMap configuration = new TreeMap();
            configuration.putAll(service.getConfiguration());
            Set<String> keys = configuration.keySet();
            for (String key : keys) {
                HashMap kv = new HashMap();
                kv.put("key", key);
                kv.put("value", WordUtils.wrap((String) configuration.get(key), 100, "<br/>", true));
                response.getList().add(kv);
            }
            return response;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @RequestMapping(value = "/configuration/widget", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response configurationWidget() {

        Response response = new Response();

        try {
            Map configuration = service.getConfiguration();

            response.getMap().put("yarnNodemanagerResourceMemoryMB", Integer.parseInt(configuration.get("yarn.nodemanager.resource.memory-mb").toString()));
            response.getMap().put("yarnSchedulerMinimumAllocationMB", Integer.parseInt(configuration.get("yarn.scheduler.minimum-allocation-mb").toString()));
            response.getMap().put("yarnSchedulerMaximumAllocationMB", Integer.parseInt(configuration.get("yarn.scheduler.maximum-allocation-mb").toString()));
            response.getMap().put("yarnNodemanagerResourceCpuVcores", Integer.parseInt(configuration.get("yarn.nodemanager.resource.cpu-vcores").toString()));
            response.getMap().put("yarnSchedulerMinimumAllocationVcores", Integer.parseInt(configuration.get("yarn.scheduler.minimum-allocation-vcores").toString()));
            response.getMap().put("yarnSchedulerMaximumAllocationVcores", Integer.parseInt(configuration.get("yarn.scheduler.maximum-allocation-vcores").toString()));

            return response;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @RequestMapping(value = "/queues", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response queues(@RequestParam String node) {

        Response response = new Response();

        try {
            List allQueues = service.getAllQueues();
            for (Object obj : allQueues) {
                Map queue = (Map) obj;
                String name = (String) queue.get("name");
                queue.put("name", org.apache.commons.lang.StringUtils.splitPreserveAllTokens(name, ".")[1]);
                queue.put("text", org.apache.commons.lang.StringUtils.splitPreserveAllTokens(name, ".")[1]);
                queue.put("queue", name);
                queue.put("leaf", true);
            }

            response.setList(allQueues);
            response.setTotal(response.getList().size());

            return response;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }


    }

    @RequestMapping(value = "/app/move", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response appToQueue(@RequestParam String applicationId,
                               @RequestParam String queue) {
        Response response = new Response();
        try {
            service.moveApplicationAcrossQueues(applicationId, queue);

            return response;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @RequestMapping(value = "/apps/running", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response getRunningApplications() {
        Response response = new Response();

        try {
            response.getList().addAll(service.getRunningApplications());
            response.setTotal(response.getList().size());

            return response;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @RequestMapping(value = "/apps/all", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response getAllApplications() {
        Response response = new Response();

        try {
            response.getList().addAll(service.listApplications(null, null, true));
            response.setTotal(response.getList().size());

            return response;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @RequestMapping(value = "/apps/types", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response getTypesAggr() {
        Response response = new Response();

        try {
            List<Map> columnList = new ArrayList<>();
            List<Map> appList = service.listApplications(null, null, true);
            List<String> keyList = new ArrayList<>();
            Map aggrMap = new HashMap();
            String type = null;

            for (Map app : appList) {
                type = app.get("applicationType").toString();
                if (aggrMap.get(type) == null) {
                    aggrMap.put(type, 1);
                    keyList.add(type);
                }
                else {
                    aggrMap.put(type, (int) aggrMap.get(type) + 1);
                }
            }

            Map column;
            for (String key : keyList) {
                column = new HashMap();

                column.put("name", key);
                column.put("y", aggrMap.get(key));

                columnList.add(column);
            }

            response.getMap().put("series", columnList);
            response.setTotal(response.getList().size());

            return response;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @RequestMapping(value = "/apps/timeseries", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response timeseries() {

        try {
            Response response = new Response();
            response.setSuccess(true);

            // FIXME : to MyBATIS, Remote
            ApplicationContext applicationContext = ApplicationContextRegistry.getApplicationContext();
            JdbcTemplate jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
            String query = "SELECT (@row:=@row+1) as num, COUNT(*) as sum, DATE_FORMAT(MAX(START_TIME),'%Y-%m-%d %H') as time, START_TIME FROM FL_CL_YARN_DUMP, (SELECT @row := 0) r WHERE SYSTEM ='{}' AND START_TIME > DATE_ADD(now(), INTERVAL -7 DAY) GROUP BY DATE_FORMAT(START_TIME,'%Y-%m-%d %H') ORDER BY START_TIME asc";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(query);
            response.getList().addAll(list);
            return response;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @RequestMapping(value = "/app/statinfo", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response appStatInfo(@RequestParam String applicationId) {

        Response response = new Response();
        response.setSuccess(true);
        response.getMap().putAll(service.getAppStatInfo(applicationId));
        return response;
    }

    /**
     * 애플리케이션의 기본 요약 정보를 가져온다.
     * Application Master 정보는 Tracking URL에서 추적할 수 있으며
     * MapReduce, Spark 등의 Application Type에 따라서 Application Master는 다르게 설정된다.
     * MapReduce의 경우 Application Master를 통해서 실행 정보를 추적할 수 있으며 Spark의 경우 현재 불가능한다.
     *
     * @param applicationId 애플리케이션 ID
     */
    @RequestMapping(value = "/app/report", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response applicationReport(@RequestParam String applicationId) {
        Response response = new Response();

        try {
            Map reportMap = service.getApplicationReport(applicationId);
            List<Map> reportList = new ArrayList<>();
            Object keys[] = reportMap.keySet().toArray();
            Map keyValueMap;

            for (Object key : keys) {
                keyValueMap = new HashMap();

                keyValueMap.put("key", key.toString());
                keyValueMap.put("value", reportMap.get(key.toString()));

                reportList.add(keyValueMap);
            }

            Collections.sort(reportList, new KeyValueComparator());

            response.getList().addAll(reportList);
            response.setTotal(response.getList().size());

            return response;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    /**
     * 애플리케이션의 컨테이너 로그를 Resource Manager에서 다운로드하여 클라이언트로 전송한다.
     * 클라이언트에서는 다운로드가 진행된다.
     *
     * @param applicationId 애플리케이션 ID
     * @param appOwner      애플리케이션을 실행한 소유자
     */
    @RequestMapping(value = "/app/download", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity downloadLog(HttpServletResponse res, @RequestParam String applicationId, @RequestParam String appOwner) {

        try {
            String applicationLog = service.getApplicationLog(applicationId, appOwner);
            byte[] bytes = applicationLog.getBytes();

            res.setHeader("Content-Length", "" + bytes.length);
            res.setHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            res.setHeader("Content-Disposition", MessageFormatter.format("attachment; path={}; filename={}", applicationId, applicationId + ".log").getMessage());
            res.setStatus(200);
            FileCopyUtils.copy(bytes, res.getOutputStream());
            res.flushBuffer();
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception ex) {
            throw new WholeBodyException("You can not handle the Application Log.", ex);
        }
    }

    /**
     * 선택한 애플리케이션을 강제 종료한다.
     *
     * @param applicationId 애플리케이션 ID
     */
    @RequestMapping(value = "/app/kill", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response kill(@RequestParam String applicationId) {

        Response response = new Response();
        try {
            service.killApplication(applicationId);

            return response;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    /**
     * 애플리케이션의 컨테이너 로그를 Resource Manager에서 다운로드하여 클라이언트로 전송한다.
     *
     * @param applicationId 애플리케이션 ID
     * @param appOwner      애플리케이션을 실행한 소유자
     * @return 애플리케이션의 컨테이너 로그
     */
    @RequestMapping(value = "/app/log", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public String applicationLog(@RequestParam String applicationId,
                                                 @RequestParam String appOwner) {

        try {
            String applicationLog = service.getApplicationLog(applicationId, appOwner);

            return applicationLog;
        } catch (Exception ex) {
            throw new WholeBodyException("Unable to get the Application Log.", ex);
        }
    }

    @RequestMapping(value = "/jvmheap", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response jvmheap() {
        Map<String, Object> jvmHeap = service.getJVMHeap();
        Response response = new Response();
        // FIXME response.getList().add(getItem(jvmHeap, "maxMemory", "Max"));
        response.getList().add(getItem(jvmHeap, "freeMemory", "Free"));
        response.getList().add(getItem(jvmHeap, "usedMemory", "Used"));
        return response;
    }

    @RequestMapping(value = "/runningMRJobs", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response runningMRJobs() {
        Response response = new Response();

        try {
            response.getList().addAll(service.getRunningMRJobs());
            response.setTotal(response.getList().size());

            return response;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    private Map getItem(Map<String, Object> values, String key, String title) {
        Map map = new HashMap();
        map.put("name", title);
        map.put("value", values.get(key));
        return map;
    }

    private class KeyValueComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Map map1 = (Map) o1;
            Map map2 = (Map) o2;

            return map1.get("key").toString().compareTo(map2.get("key").toString());
        }
    }
}