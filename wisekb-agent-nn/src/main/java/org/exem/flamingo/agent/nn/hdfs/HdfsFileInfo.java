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
package org.exem.flamingo.agent.nn.hdfs;

import org.apache.commons.lang.SystemUtils;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileStatus;
import org.exem.flamingo.shared.model.rest.FileInfo;

/**
 * HDFS의 파일 메타 정보.
 *
 * @author Byoung Gon, Kim
 * @since 0.1
 */
public class HdfsFileInfo implements FileInfo {

    private static final long serialVersionUID = 1;

    private String filename;

    private String fullyQualifiedPath;

    private String path;

    private long length;

    private boolean directory;

    private boolean file;

    private String owner;

    private String group;

    private long blockSize;

    private int replication;

    private long modificationTime;

    private long accessTime;

    private String permission;

    private long spaceQuota;

    private long spaceConsumed;

    private long quota;

    private long fileCount;

    private long directoryCount;

    public HdfsFileInfo(FileStatus fileStatus, ContentSummary contentSummary) {
        this.fullyQualifiedPath = fileStatus.getPath().toUri().getPath();
        this.filename = isEmpty(getFilename(fullyQualifiedPath)) ? getDirectoryName(fullyQualifiedPath) : getFilename(fullyQualifiedPath);
        this.length = fileStatus.isFile() ? fileStatus.getLen() : contentSummary.getLength();
        this.path = getPath(fullyQualifiedPath);
        this.directory = fileStatus.isDirectory();
        this.file = !fileStatus.isDirectory();
        this.owner = fileStatus.getOwner();
        this.group = fileStatus.getGroup();
        this.blockSize = fileStatus.getBlockSize();
        this.replication = fileStatus.getReplication();
        this.modificationTime = fileStatus.getModificationTime();
        if (contentSummary != null) {
            this.spaceConsumed = contentSummary.getSpaceConsumed();
            this.spaceQuota = contentSummary.getSpaceQuota();
            this.quota = contentSummary.getQuota();
            this.directoryCount = contentSummary.getDirectoryCount();
            this.fileCount = contentSummary.getFileCount();
        }
        this.accessTime = fileStatus.getAccessTime();
        this.permission = fileStatus.getPermission().toString();
    }

    public static String getPath(String fullyQualifiedPath) {
        int sep = fullyQualifiedPath.lastIndexOf(SystemUtils.FILE_SEPARATOR);
        if (sep != 0) {
            return fullyQualifiedPath.substring(0, sep);
        }
        return SystemUtils.FILE_SEPARATOR;
    }

    public static String getDirectoryName(String fullyQualifiedPath) {
        int sep = fullyQualifiedPath.lastIndexOf(SystemUtils.FILE_SEPARATOR);
        int length = fullyQualifiedPath.getBytes().length;
        return fullyQualifiedPath.substring(sep + 1, length);
    }

    public static String getFilename(String path) {
        if (path == null) {
            return null;
        } else {
            int separatorIndex = path.lastIndexOf("/");
            return separatorIndex != -1 ? path.substring(separatorIndex + 1) : path;
        }
    }

    public static boolean isEmpty(String str) {
        return (str == null || str.trim().length() < 1);
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String getFullyQualifiedPath() {
        return fullyQualifiedPath;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean isFile() {
        return file;
    }

    @Override
    public boolean isDirectory() {
        return directory;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public long getBlockSize() {
        return blockSize;
    }

    @Override
    public int getReplication() {
        return replication;
    }

    @Override
    public long getModificationTime() {
        return modificationTime;
    }

    @Override
    public long getAccessTime() {
        return accessTime;
    }

    public String getPermission() {
        return permission;
    }

    public void setSpaceQuota(long spaceQuota) {
        this.spaceQuota = spaceQuota;
    }

    public long getSpaceQuota() {
        return spaceQuota;
    }

    public void setSpaceConsumed(long spaceConsumed) {
        this.spaceConsumed = spaceConsumed;
    }

    public long getSpaceConsumed() {
        return spaceConsumed;
    }

    public void setQuota(long quota) {
        this.quota = quota;
    }

    public long getQuota() {
        return quota;
    }

    public void setFileCount(long fileCount) {
        this.fileCount = fileCount;
    }

    public long getFileCount() {
        return fileCount;
    }

    public void setDirectoryCount(long directoryCount) {
        this.directoryCount = directoryCount;
    }

    public long getDirectoryCount() {
        return directoryCount;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setFullyQualifiedPath(String fullyQualifiedPath) {
        this.fullyQualifiedPath = fullyQualifiedPath;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setBlockSize(long blockSize) {
        this.blockSize = blockSize;
    }

    public void setReplication(int replication) {
        this.replication = replication;
    }

    public void setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
    }

    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
