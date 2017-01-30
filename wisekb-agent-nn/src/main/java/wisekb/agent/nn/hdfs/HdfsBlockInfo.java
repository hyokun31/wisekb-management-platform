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
package wisekb.agent.nn.hdfs;

import java.util.List;

/**
 * HDFS Block의 메타 정보
 *
 * @author Kyeong Sik, Kim
 * @since 0.1
 */
public class HdfsBlockInfo {
    private long blockSize;

    private int liveReplicas;

    private List<String> replicationNodeList;

    private long blockId;

    private String blockName;

    private String blockPoolId;

    private long generationStamp;

    public long getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(long blockSize) {
        this.blockSize = blockSize;
    }

    public int getLiveReplicas() {
        return liveReplicas;
    }

    public void setLiveReplicas(int liveReplicas) {
        this.liveReplicas = liveReplicas;
    }

    public List<String> getReplicationNodeList() {
        return replicationNodeList;
    }

    public void setReplicationNodeList(List<String> replicationNodeList) {
        this.replicationNodeList = replicationNodeList;
    }

    public long getBlockId() {
        return blockId;
    }

    public void setBlockId(long blockId) {
        this.blockId = blockId;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getBlockPoolId() {
        return blockPoolId;
    }

    public void setBlockPoolId(String blockPoolId) {
        this.blockPoolId = blockPoolId;
    }

    public long getGenerationStamp() {
        return generationStamp;
    }

    public void setGenerationStamp(long generationStamp) {
        this.generationStamp = generationStamp;
    }

    @Override
    public String toString() {
        return "HdfsBlockInfo{" +
                "blockSize=" + blockSize +
                ", liveReplicas=" + liveReplicas +
                ", replicationNodeList=" + replicationNodeList +
                ", blockId=" + blockId +
                ", blockName=" + blockName +
                ", blockPoolId=" + blockPoolId +
                ", generationStamp=" + generationStamp +
                '}';
    }
}
