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
package org.exem.flamingo.shared.util.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.hadoop.hdfs.server.namenode.ha.proto.HAZKInfoProtos;
import org.apache.hadoop.yarn.proto.YarnServerResourceManagerServiceProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkHaUtils {

    private static Logger logger = LoggerFactory.getLogger(ZkHaUtils.class);

    public static String getActiveNnHost(String zkQuorum, String znode) {

        CuratorFramework client = ZkUtils.createClient(zkQuorum);

        client.start();

        try {
            byte[] bytes = ZkUtils.getData(client, znode);

            HAZKInfoProtos.ActiveNodeInfo proto = HAZKInfoProtos.ActiveNodeInfo.parseFrom(bytes);

            logger.info("Active namenode is " + proto.getHostname());

            return proto.getHostname();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            client.close();
        }
    }

    public static String getActiveRmHost(String zkQuorum, String znode) {
        CuratorFramework client = ZkUtils.createClient(zkQuorum);

        client.start();

        try {
            byte[] bytes = ZkUtils.getData(client, znode);

            YarnServerResourceManagerServiceProtos.ActiveRMInfoProto proto = YarnServerResourceManagerServiceProtos.ActiveRMInfoProto.parseFrom(bytes);

            logger.info("Active resourcemanager is " + proto.getRmId());

            return proto.getRmId();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            client.close();
        }
    }
}
