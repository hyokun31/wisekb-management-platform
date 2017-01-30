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
import org.exem.flamingo.shared.model.rest.FileInfo;
import org.exem.flamingo.shared.util.ExceptionUtils;
import org.exem.flamingo.shared.util.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.BlockReader;
import org.apache.hadoop.hdfs.DFSClient;
import org.apache.hadoop.hdfs.RemoteBlockReader2;
import org.apache.hadoop.hdfs.net.Peer;
import org.apache.hadoop.hdfs.protocol.*;
import org.apache.hadoop.hdfs.security.token.block.BlockTokenIdentifier;
import org.apache.hadoop.hdfs.server.common.HdfsServerConstants;
import org.apache.hadoop.hdfs.server.datanode.CachingStrategy;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.security.token.Token;

import org.exem.flamingo.agent.nn.hdfs.HdfsFileInfo;
import org.exem.flamingo.agent.nn.hdfs.HdfsFileOnlyInfo;

import org.exem.flamingo.shared.util.HdfsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.*;

/**
 * HDFS 브라우저의 HDFS 관리 기능을 Namenode Agnet에서 제공하는 HDFS File System Provider.
 *
 * @author Myeong Ha, Kim
 * @since 0.1
 */
public class FileSystemProvider {

    /**
     * HDFS Default Chunk Size To View
     */
    private static final Long DEFAULT_CHUNK_SIZE = (long) 32768;

    /**
     * SLF4J Application Logging
     */
    private Logger logger = LoggerFactory.getLogger(FileSystemProvider.class);

    /**
     * SLF4J Exception Logging
     */
    private Logger exceptionLogger = LoggerFactory.getLogger("flamingo.exception");

    static Random rand = new Random();

    /**
     * 경로에 해당하는 디렉토리 또는 파일 목록을 가져온다.
     *
     * @param path          HDFS의 파일 또는 디렉토리
     * @param directoryOnly 디렉토리 및 파일 유무
     * @return 디렉토리 목록 or 파일 목록
     */
    public List<FileInfo> list(String path, boolean directoryOnly) {
        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);

            FileStatus[] files;
            List<FileInfo> fileInfoList = new ArrayList<>();

            if (directoryOnly) {
                files = fs.listStatus(new Path(path));
                fileInfoList = new ArrayList<>(files.length);
                for (FileStatus file : files) {
                    try {
                        if (file.isDirectory()) {
                            fileInfoList.add(new HdfsFileInfo(file, fs.getContentSummary(file.getPath())));
                        }
                    } catch (Exception ex) {
                        fileInfoList.add(new HdfsFileInfo(file, null));
                    }
                }
            } else {
                RemoteIterator<LocatedFileStatus> locatedFileStatusIterator = fs.listFiles(new Path(path), false);
                while (locatedFileStatusIterator.hasNext()) {
                    LocatedFileStatus locatedFileStatus = locatedFileStatusIterator.next();
                    fileInfoList.add(new HdfsFileOnlyInfo(locatedFileStatus, fs.getContentSummary(locatedFileStatus.getPath())));
                }
            }

            return fileInfoList;
        } catch (Exception ex) {
            throw new ServiceException("디렉토리 목록을 확인할 수 없습니다.", ex);
        }
    }

    /**
     * 선택한 경로에 디렉토리 및 파일을 생성한다.
     *
     * @param srcPath   HDFS의 파일 또는 디렉토리
     * @param username  Username
     * @return true or false
     */
    public boolean mkdir(String srcPath, String username) {
        if (!FileUtils.pathValidator(srcPath)) {
            throw new ServiceException("Invalid path. Please check the path.");
        }

        if (exists(srcPath)) {
            throw new ServiceException("선택한 경로에 동일한 디렉토리가 존재합니다.");
        }

        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);

            if (fs.mkdirs(new Path(srcPath))) {
                setOwnership(new Path(srcPath), username, username);
            }
            return true;
        } catch (IOException ex) {
            throw new ServiceException("디렉토리를 생성할 수 없습니다.", ex);
        }
    }

    /**
     * 시작 경로에서 목적지 경로로 디렉토리 및 파일을 복사한다.
     *
     * @param srcPath   시작 경로
     * @param dstPath   목적지 경로
     * @param username  Username
     * @return true or false
     */
    public boolean copy(String srcPath, String dstPath, String username) {
        if (!FileUtils.pathValidator(srcPath) || !FileUtils.pathValidator(dstPath)) {
            throw new ServiceException("Invalid path. Please check the path.");
        }

        if (!exists(srcPath)) {
            throw new ServiceException("복사할 디렉토리 또는 파일이 존재하지 않습니다.");
        }

        if (exists(dstPath))
            throw new ServiceException("복사할 경로에 동일한 이름의 디렉토리 또는 파일이 존재합니다.");
        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);

            if (fs.isFile(new Path(srcPath))) {
                FSDataInputStream fis = fs.open(new Path(srcPath));
                FSDataOutputStream fos = fs.create(new Path(dstPath));

                FileCopyUtils.copy(fis, fos);

                IOUtils.closeQuietly(fos);
                IOUtils.closeQuietly(fis);

                setOwnership(new Path(srcPath), username, username);
            } else {
                FileUtil.copy(fs, new Path(srcPath), fs, new Path(dstPath), false, new Configuration());
            }

            return true;
        } catch (Exception ex) {
            throw new ServiceException("디렉토리 또는 파일을 복사할 수 없습니다.", ex);
        }
    }


    /**
     * 시작 경로에서 목적지 경로로 디렉토리 및 파일을 이동한다.
     *
     * @param srcPath 시작 경로
     * @param dstPath 목적지 경로
     * @return true or false
     */
    public boolean move(String srcPath, String dstPath) {
        if (!FileUtils.pathValidator(srcPath) || !FileUtils.pathValidator(dstPath)) {
            throw new ServiceException("Invalid path. Please check the path.");
        }

        if (!exists(srcPath)) {
            throw new ServiceException("이동할 디렉토리 또는 파일이 존재하지 않습니다.");
        }

        if (!getFileInfo(FileUtils.getPath(dstPath)).isDirectory()) {
            throw new ServiceException("이동할 경로의 속성이 파일입니다.");
        }

        if (exists(dstPath)) {
            throw new ServiceException("이동할 경로에 디렉토리 또는 파일이 존재합니다.");
        }

        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);
            Path from = new Path(srcPath);
            Path to = new Path(dstPath);

            return fs.rename(from, to);
        } catch (Exception ex) {
            throw new ServiceException("디렉토리 또는 파일을 이동할 수 없습니다.");
        }
    }

    /**
     * 선택한 디렉토리명 또는 파일명을 변경한다.
     *
     * @param srcPath           원본 디렉토리 또는 파일의 전체 경로
     * @param name              변경될 디렉토리명 또는 파일명
     * @return true or false
     */
    public boolean rename(String srcPath, String name) {
        if (!FileUtils.pathValidator(srcPath)) {
            throw new ServiceException("Invalid path. Please check the path.");
        }

        Path oldSrcPath = new Path(srcPath);
        Path newDstPath = new Path(FileUtils.getPath(srcPath), name);

        if (exists(newDstPath.toString())) {
            throw new ServiceException("선택한 경로에 동일한 디렉토리 또는 파일이 존재합니다.");
        }

        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);

            return fs.rename(oldSrcPath, newDstPath);
        } catch (Exception ex) {
            throw new ServiceException("디렉토리명을 변경할 수 없습니다.", ex);
        }
    }

    /**
     * HDFS의 파일 또는 디렉토리를 삭제한다.
     *
     * @param path HDFS의 파일 또는 디렉토리
     * @return 정상적으로 삭제된 경우 <tt>true</tt>, 그렇지 않은 경우는 <tt>false</tt>
     * @throws ServiceException 파일 시스템에 접근할 수 없는 경우
     */
    public boolean delete(String path) {
        if (!FileUtils.pathValidator(path)) {
            throw new ServiceException("Invalid path. Please check the path.");
        }

        if ("/".equals(path)) {
            throw new ServiceException("루트는 삭제할 수 없습니다.");
        }

        if (!exists(path)) {
            throw new ServiceException("디렉토리 또는 파일이 존재하지 않습니다.");
        }

        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);

            return fs.delete(new Path(path), true);
        } catch (Exception ex) {
            throw new ServiceException("디렉토리를 삭제할 수 없습니다.", ex);
        }
    }

    /**
     * HDFS의 파일들을 병합한다.
     *
     * @param srcPath   HDFS의 디렉토리 경로
     * @param dstPath   HDFS의 병합된 파일이 저장될 경로
     * @param username  사용자명
     * @return 정상적으로 삭제된 경우 <tt>true</tt>, 그렇지 않은 경우는 <tt>false</tt>
     * @throws ServiceException 파일 시스템에 접근할 수 없는 경우
     */
    public boolean merge(String srcPath, String dstPath, String username) {
        if (!FileUtils.pathValidator(srcPath) || !FileUtils.pathValidator(dstPath)) {
            throw new ServiceException("Invalid path. Please check the path.");
        }

        if (!getFileInfo(srcPath).isDirectory()) {
            throw new ServiceException("선택한 노드의 속성이 파일입니다.");
        }

        if (exists(dstPath)) {
            throw new ServiceException("선택한 경로에 동일한 디렉토리 또는 파일이 존재합니다.");
        }

        try {
            Path src = new Path(srcPath);
            Path dst = new Path(dstPath);
            FileSystem fs = src.getFileSystem(Namenode2Agent.configuration);
            Path[] srcs = FileUtil.stat2Paths(fs.globStatus(src), src);

            for (Path source : srcs) {
                FileUtil.copyMerge(fs, source, fs, dst, false, fs.getConf(), null);
            }

            setOwnership(new Path(dstPath), username, username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * HDFS의 디렉토리 또는 파일 정보를 조회한다.
     *
     * @param path HDFS의 디렉토리 또는 파일 경로
     * @return 정상적으로 조회된 경우 <tt>FileInfo</tt>, 그렇지 않은 경우는 <tt>false</tt>
     * @throws ServiceException 파일 시스템에 접근할 수 없는 경우
     */
    public HdfsFileInfo getFileInfo(String path) {
        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);
            FileStatus fileStatus = fs.getFileStatus(new Path(path));
            ContentSummary summary = fs.getContentSummary(new Path(path));
            HdfsFileInfo hdfsFileInfo = new HdfsFileInfo(fileStatus, summary);

            hdfsFileInfo.setDirectory(fileStatus.isDirectory());
            hdfsFileInfo.setBlockSize(fileStatus.getBlockSize());
            hdfsFileInfo.setReplication(fileStatus.getReplication());
            hdfsFileInfo.setDirectoryCount(summary.getDirectoryCount());
            hdfsFileInfo.setFileCount(summary.getFileCount());
            hdfsFileInfo.setQuota(summary.getQuota());
            hdfsFileInfo.setSpaceQuota(summary.getSpaceQuota());
            hdfsFileInfo.setSpaceConsumed(summary.getSpaceConsumed());
            hdfsFileInfo.setLength(fileStatus.getLen());

            return hdfsFileInfo;
        } catch (Exception ex) {
            throw new ServiceException("디렉토리 또는 파일 정보를 가져올 수 없습니다.", ex);
        }
    }

    /**
     * HDFS에 업로드한 파일을 저장한다.
     *
     * @param pathToUpload          업로드한 파일을 저장할 전체 경로
     * @param fullyQualifiedPath    업로드한 파일을 저장할 전체 경로
     * @param content               저장할 파일의 바이트 배열
     * @param username              Username
     * @return 저장 여부
     */
    public boolean save(String pathToUpload, String fullyQualifiedPath, byte[] content, String username) {
        InputStream is = new ByteArrayInputStream(content);
        OutputStream os = null;

        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);

            if (!FileUtils.pathValidator(pathToUpload) || !FileUtils.pathValidator(fullyQualifiedPath)) {
                throw new ServiceException("Invalid path. Please check the path.");
            }

            if (!fs.exists(new Path(pathToUpload))) {
                throw new ServiceException("업로드할 경로가 존재하지 않습니다.");
            }

            if (fs.exists(new Path(fullyQualifiedPath))) {
                throw new ServiceException("파일이 이미 존재합니다.");
            }

            os = fs.create(new Path(fullyQualifiedPath));
            FileCopyUtils.copy(is, os);
            setOwnership(new Path(fullyQualifiedPath), username, username);

            return true;
        } catch (Exception ex) {
            throw new ServiceException("파일을 저장할 수 없습니다.", ex);
        } finally {
            try {
                if (os != null) os.close();
            } catch (Exception ex) {
                // Ignored
            }

            try {
                is.close();
            } catch (Exception ex) {
                // Ignored
            }
        }
    }

    public byte[] load(String fullyQualifiedPath) {
        InputStream content = null;
        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);

            if (!FileUtils.pathValidator(fullyQualifiedPath)) {
                throw new ServiceException("Invalid path. Please check the path.");
            }

            if (fs.exists(new Path(fullyQualifiedPath))) {
                throw new ServiceException("다운로드할 파일이 존재하지 않습니다.");
            }

            content = getContent(fullyQualifiedPath);
            return FileCopyUtils.copyToByteArray(content);
        } catch (Exception ex) {
            throw new ServiceException("선택한 파일을 로딩할 수 없습니다.", ex);
        } finally {
            IOUtils.closeQuietly(content);
        }
    }

    public InputStream getContent(String path) {
        if (!exists(path) || !getFileInfo(path).isFile()) {
            throw new ServiceException("선택한 파일은 존재하지 않거나 파일이 아닙니다.");
        }

        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);

            return HdfsUtils.getInputStream(fs, path);
        } catch (Exception ex) {
            throw new ServiceException("선택한 파일의 입력 스트링을 얻을 수 없습니다. 파일 시스템을 확인해 주십시오.", ex);
        }
    }

    /**
     * 사용자가 선택한 파일의 내용을 지정된 범위에 따라 페이징 처리 후 결과를 전달한다.
     *
     * @param contentsMap {
     *
     *                          bestNode                    파일 내용을 읽어올 데이터 노드의 위치
     *                          buttonType                  페이징 버튼 종류
     *                          chunkSizeToView             한 페이지당 읽어올 파일 내용 범위
     *                          clusterName                 클러스터명
     *                          currentContentsBlockSize    현재 파일 내용을 구성하는 Block Size
     *                          currentPage                 현재 페이지
     *                          dfsBlockSize                DFS Block Size
     *                          dfsBlockStartOffset         DFS Block Start Offset
     *                          filePath                    파일 경로
     *                          fileSize                    파일 전체 크기
     *                          lastDfsBlockSize            Last DFS Block Size
     *                          startOffset                 Start Offset
     *                          totalPage                   전체 페이지
     *                    }
     * @return contentsMap
     */
    public Map view(Map contentsMap) {

        try {
            String filePath = (String) contentsMap.get("filePath");
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);
            ContentSummary summary = fs.getContentSummary(new Path(filePath));
            long fileSize = summary.getLength();
            long dfsBlockSize = Long.parseLong(String.valueOf(contentsMap.get("dfsBlockSize")));
            long startOffset = Long.parseLong(String.valueOf(contentsMap.get("startOffset")));
            long dfsBlockStartOffset = Long.parseLong(String.valueOf(contentsMap.get("dfsBlockStartOffset")));
            int currentContentsBlockSize = Integer.parseInt(String.valueOf(contentsMap.get("currentContentsBlockSize")));
            int currentPage = (int) contentsMap.get("currentPage");
            int totalPage = Integer.parseInt(String.valueOf(contentsMap.get("totalPage")));
            String buttonType = (String) contentsMap.get("buttonType");
            long chunkSizeToView = contentsMap.containsKey("chunkSizeToView") ?
                    Long.parseLong(String.valueOf(contentsMap.get("chunkSizeToView"))) : DEFAULT_CHUNK_SIZE;
            long lastDfsBlockSize = 0;

            if (fileSize > dfsBlockSize) {
                if (contentsMap.containsKey("lastDfsBlockSize")) {
                    lastDfsBlockSize = Long.parseLong(String.valueOf(contentsMap.get("lastDfsBlockSize")));
                }
            }

            DFSClient dfsClient = new DFSClient(fs.getUri(), Namenode2Agent.configuration);

            if (!FileUtils.pathValidator(filePath)) {
                throw new ServiceException("Invalid path. Please check the path.");
            }

            if (chunkSizeToView <= 0) {
                chunkSizeToView = DEFAULT_CHUNK_SIZE;
            }

            long lastPageChunkSizeToView = fileSize % chunkSizeToView;

            if (currentPage == 0) {
                if (fileSize > chunkSizeToView) {
                    totalPage = (int) (fileSize / chunkSizeToView);
                    if (lastPageChunkSizeToView > 0) {
                        totalPage++;
                    }
                } else {
                    totalPage = 1;
                }

                if (fileSize > dfsBlockSize) {
                    long lastDfsBlockStartOffset = fileSize;
                    LocatedBlocks locatedBlocks = dfsClient.getNamenode().getBlockLocations(filePath, lastDfsBlockStartOffset, chunkSizeToView);
                    lastDfsBlockSize = locatedBlocks.getLastLocatedBlock().getBlockSize();
                    contentsMap.put("lastDfsBlockSize", lastDfsBlockSize);
                }
            }

            // 한 페이지당 페이지 사이즈(chunkSizeToView) 기준 선택한 파일의 전체 페이지 사이즈
            contentsMap.put("totalPage", totalPage);

            // BlockPool을 구성하는 DFS Block의 총 개수
            int dfsBlockCount = (int) (fileSize / dfsBlockSize);
            long dfsBlockResidue = fileSize / dfsBlockSize;
            if (dfsBlockResidue > 0) {
                dfsBlockCount++;
            }

            int moveToPage;
            long viewSize = chunkSizeToView; // File contents range to view for DFS Block in BlockPool

            /**
             * CurrentPage의 경우 첫 페이지 및 FirstButton을 제외한 모든 페이지의 값이 0보다 커야 한다.
             *
             * Case 1. Next Button
             * Case 1.1. 다음 페이지로 이동할 경우
             * Case 1.2. 마지막 페이지로 이동할 경우
             *
             * Case 2. Last Button
             * Case 2.1. 마지막 페이지로 이동할 경우
             *
             * Case 3. Previous Button
             * Case 3.1. 이전 페이지로 이동할 경우
             * Case 3.2. 마지막 페이지에서 이전 페이지로 이동할 경우
             * Case 3.2.1 이동할 페이지가 첫 페이지일 경우
             * Case 3.2.2 이동할 페이지가 첫 페이지가 아닐 경우
             *
             * Case 4 Custom Page
             * Case 4.1. 첫 페이지로 이동할 경우
             * Case 4.2. 특정 페이지로 이동할 경우
             * Case 4.2. 마지막 페이지로 이동할 경우
             *
             * Case 5. Default Page
             * Case 5.1 첫 페이지를 열거나 첫 페이지로 이동할 경우
             */
            switch (buttonType) {
                case "nextButton":
                    moveToPage = currentPage + 1;
                    if (moveToPage < totalPage) {
                        startOffset += chunkSizeToView;
                    } else if (moveToPage == totalPage) {
                        startOffset = fileSize - lastPageChunkSizeToView;
                        viewSize = lastPageChunkSizeToView;
                    }
                    break;
                case "lastButton":
                    moveToPage = totalPage;
                    startOffset = fileSize - lastPageChunkSizeToView;
                    viewSize = lastPageChunkSizeToView;
                    break;
                case "prevButton":
                    moveToPage = currentPage - 1;
                    if (currentPage < totalPage) {
                        startOffset -= chunkSizeToView;
                    } else if (currentPage == totalPage) {
                        if (moveToPage == 1) {
                            startOffset = 0;
                        } else {
                            startOffset -= chunkSizeToView;
                        }
                    }
                    break;
                case "customPage":
                    moveToPage = currentPage;
                    if (moveToPage == 1) {
                        startOffset = (long) 0;
                    } else if (moveToPage < totalPage) {
                        startOffset = chunkSizeToView * moveToPage;
                    } else if (moveToPage == totalPage) {
                        startOffset = fileSize - lastPageChunkSizeToView;
                        viewSize = lastPageChunkSizeToView;
                    }
                    break;
                default:
                    moveToPage = 1;
                    startOffset = (long) 0;
                    // 첫 페이지가 chunkSizeToView 값보다 적을 경우 파일 사이즈만큼만 읽어옴.
                    if (fileSize < chunkSizeToView) {
                        viewSize = fileSize;
                    }
                    break;
            }

            // 폐이징 처리에 반드시 필요한 파라미터
            contentsMap.put("currentPage", moveToPage);
            contentsMap.put("startOffset", startOffset);

            /**
             * 선택한 파일에 대해 네임노드에 저장된 메타데이터 정보를 검색 후
             * 데이타노드에 파일이 위치한 정보(fileSize, blockSize, blockCount, genStamp, location...)를 가져온다.
             * 각 블럭의 데이타노드 위치는 DFS Client 주소 패턴으로 정렬되어 리턴됨.
             * 전체 DFS Pool에서 startOffset 값과 Pool에 속한 각각의 DFS 블럭에 사용되는 startOffset 값은 파일 사이즈가 블럭 사이즈보다 클 경우 달라진다.
             */
            LocatedBlocks locatedBlocks = dfsClient.getNamenode().getBlockLocations(filePath, startOffset, viewSize);
            int nextContentsBlockSize = locatedBlocks.locatedBlockCount();

            // DFS Block Size 범위에서 chunkSizeToView 크기에 따라 볼 수 있는 페이징 개수
            long dfsBlockViewCount = dfsBlockSize / chunkSizeToView;
            long dfsBlockViewResidueSize = dfsBlockSize % chunkSizeToView;
            if (dfsBlockViewResidueSize > 0) {
                dfsBlockViewCount++;
            }

            List<Long> startOffsetPerDfsBlocks = new ArrayList<>();
            List<Long> accumulatedStartOffsetPerDfsBlocks = new ArrayList<>();
            List<Long> lastStartOffsetPerDfsBlocks = new ArrayList<>();
            List<Long> lastChunkSizePerDfsBlocks = new ArrayList<>();
            List<Long> pageCheckPoints = new ArrayList<>();

            /**
             * 파일 사이즈가 DFS Block Size 보다 클 경우
             * 각 블럭이 끝나고 다음 블럭으로 이어지는 시점에서 Block ID가 변경됨.
             * 블럭 ID가 변경되는 startOffset 범위에서는 locatedBlockList 정보에 다음 Block ID까지 함께 전달됨.
             * 이 때 LocatedBlockSize 크기는 2.
             * 페이지 사이즈(ChunkSizeToView)에 따라 해당 페이지의 내용은 DFS Block이 바뀌는 시점에서는
             * 현재 블럭의 마지막 남은 페이지 크기(currentBlockChunkSizeToView)와
             * 다음 블럭에 사용될 첫 페이지 크기(nextBlockChunkSizeToView)의 조합으로 만들어질 수 있다.
             * 따라서 Block ID가 변경되는 페이지에서 두 Block ID의 페이지 사이즈를 계산 후
             * 각 블럭의 startOffset 값으로 파일 내용을 조회 후 Merge해야 함.
             * 전체 DFS Block Pool에서 파일의 특정 위치에서의 내용을 조회할 때 사용하는 startOffset와 각 DFS Block을 조회할 때 사용하는 startOffset는 다르다.
             *
             * DFS Block Size = 128 MB (134,217,728 B), StartOffset Range Per DFS Block = 0 ~ 134217727, ChunkSizeToView : 10000
             * ex. moveToPage == 13421, locatedBlocks size == 2
             * First DFS Block's Last StartOffset           : 134210000
             * Second DFS Block's First(Accumulated) Offset : 0 ~ 2271
             * Second DFS Block's Second StartOffset        : 2272
             * Second DFS Block's Last StartOffset          : 134212272
             * Third DFS Block's First(Accumulated) Offset  : 0 ~ 4543
             * Third DFS Block's Second StartOffset         : 4544
             */
            if (fileSize > dfsBlockSize) {
                long accumulatedStartOffset;
                long startOffsetForDfsBlock;
                long startOffsetForSecondDfsBlock = chunkSizeToView - dfsBlockViewResidueSize;
                long dfsBlockLastChunkSize = chunkSizeToView;
                for (int i = 0; i < dfsBlockCount; i++) {
                    accumulatedStartOffset = startOffsetForSecondDfsBlock * i;
                    accumulatedStartOffsetPerDfsBlocks.add(i, accumulatedStartOffset);

                    if (dfsBlockLastChunkSize < startOffsetForSecondDfsBlock) {
                        dfsBlockLastChunkSize += chunkSizeToView;
                    }

                    // 마지막 블럭은 이전 블럭들과 사이즈가 다를 수 있기 때문에 따로 처리함.
                    long lastDfsBlockLastStartOffset = 0;
                    if (i == dfsBlockCount - 1) {
                        long lastDfsBlockViewCount = lastDfsBlockSize / chunkSizeToView;
                        long lastDfsBlockResidue = lastDfsBlockSize % chunkSizeToView;

                        if (lastDfsBlockResidue < dfsBlockLastChunkSize) {
                            lastDfsBlockViewCount--;
                        }

                        lastDfsBlockLastStartOffset = (lastDfsBlockViewCount * chunkSizeToView) + (chunkSizeToView - dfsBlockLastChunkSize); //47841808
                        dfsBlockLastChunkSize = lastDfsBlockSize - lastDfsBlockLastStartOffset;
                    } else {
                        dfsBlockLastChunkSize -= startOffsetForSecondDfsBlock;
                    }
                    lastChunkSizePerDfsBlocks.add(i, dfsBlockLastChunkSize);

                    long dfsBlockLastStartOffset;
                    if (i == dfsBlockCount - 1) {
                        dfsBlockLastStartOffset = lastDfsBlockLastStartOffset;
                    } else {
                        dfsBlockLastStartOffset = dfsBlockSize - dfsBlockLastChunkSize;
                    }
                    lastStartOffsetPerDfsBlocks.add(i, dfsBlockLastStartOffset);

                    startOffsetForDfsBlock = dfsBlockLastStartOffset % chunkSizeToView;
                    startOffsetPerDfsBlocks.add(i, startOffsetForDfsBlock);
                }

                // 각 DFS Block의 위치를 결정하는데 반드시 필요한 변수들
                contentsMap.put("accumulatedStartOffsetPerDfsBlocks", accumulatedStartOffsetPerDfsBlocks);
                contentsMap.put("lastStartOffsetPerDfsBlocks", lastStartOffsetPerDfsBlocks);
                contentsMap.put("lastChunkSizePerDfsBlocks", lastChunkSizePerDfsBlocks);
                contentsMap.put("startOffsetPerDfsBlocks", startOffsetPerDfsBlocks);

                long firstPageCheckPoint = dfsBlockSize / chunkSizeToView;
                long pageCheckPoint = 0;
                long pageCheckChunkSizeToView = chunkSizeToView;
                for (int i = 0; i < 15; i++) {
                    pageCheckPoint += firstPageCheckPoint;
                    int j = i;
                    j++;
                    if (j < accumulatedStartOffsetPerDfsBlocks.size()) {
                        if (accumulatedStartOffsetPerDfsBlocks.get(j) > pageCheckChunkSizeToView) {
                            pageCheckChunkSizeToView += chunkSizeToView;
                            pageCheckPoint -= 1;
                        }
                        pageCheckPoints.add(i, pageCheckPoint);
                        pageCheckPoint++;
                    }
                }

                // CustomPage 처리를 위해서 각 DFS Block Size가 변경되는 페이지 정보를 저장한다.
                contentsMap.put("pageCheckPoints", pageCheckPoints);
            }

            /**
             * locatedBlocks 사이즈가 변경되는 기본 조건 : moveToPage >= dfsBlockViewCount - 1
             *
             * ex.
             * offsetRange 0    >> moveToPage < dfsBlockViewCount - 1 : 13420 - (13422-1)
             * offsetRange 1    >> moveToPage == dfsBlockViewCount - 1 : 13421 - (13422-1)
             * offsetRange 2    >> moveToPage > dfsBlockViewCount - 1 : 13422 - (13422-1)
             */
            int offsetRange = (int) (moveToPage / (dfsBlockViewCount - 1));

            LocatedBlock locatedBlock;
            LocatedBlock nextLocatedBlock = null;
            long currentBlockLastStartOffset = 0;
            long currentBlockLastChunkSizeToView = 0;
            long nextBlockFirstStartOffset = 0;
            long nextBlockFirstChunkSizeToView = 0;
            boolean splitViewFlag = false;

            /**
             * 이동할 페이지 내용이 두 개의 DFS 블럭으로 조합될 경우
             * Criteria : DFS Block Size(128MB) and ChunkSizeToView(10000B)
             *
             * 현재 블럭의 마지막 StartOffset 값과 다음 블럭의 첫번째 StartOffset(0)에서 읽어들일 ChunkSizeToView 범위를 설정함.
             * currentBlockLastStartOffset ~ nextBlockAccumulatedStartOffset
             * ex. 134210000 ~ 2272             */
            if (nextContentsBlockSize > 1) {
                splitViewFlag = true;
                locatedBlock = locatedBlocks.get(0);
                nextLocatedBlock = locatedBlocks.get(1);

                dfsBlockStartOffset = startOffsetPerDfsBlocks.get(offsetRange);
                contentsMap.put("dfsBlockStartOffset", dfsBlockStartOffset); // 다음번째 블럭의 startOffset 위치를 미리 구해서 전달

                currentBlockLastStartOffset = lastStartOffsetPerDfsBlocks.get(offsetRange - 1);
                currentBlockLastChunkSizeToView = lastChunkSizePerDfsBlocks.get(offsetRange - 1);
                nextBlockFirstStartOffset = 0;
                nextBlockFirstChunkSizeToView = chunkSizeToView - currentBlockLastChunkSizeToView;
            } else {
                locatedBlock = locatedBlocks.get(0);
            }

            // 마지막 DFS Block의 경우는 이전 블럭과 사이즈가 다를 수 있기 때문에 파라미터로 전달하지 않고 따로 처리함.
            if (offsetRange < pageCheckPoints.size()) {
                contentsMap.put("dfsBlockSize", dfsBlockSize);
            }

            // 현재 페이지가 두 개의 블럭으로 구성된 경우
            boolean currentPageSplitViewFlag = false;
            if (currentContentsBlockSize > 1) {
                currentPageSplitViewFlag = true;
            }

            /**
             * DFS1 -> DFS0 범위로 이동할 때
             * currentPageSplitViewFlag가 true 일 때 dfsBlockStartOffset 변수 사용
             * ex. 13421 -> 13420
             */
            if (moveToPage < (dfsBlockViewCount - 1) && (moveToPage + 1) == (dfsBlockViewCount - 1)) {
                dfsBlockStartOffset = startOffset;
            }

            // 첫번째 DFS Block Size를 넘어서는 페이징 범위 부터 각 DFS Block마다 누적되는 가변적인 StartOffset 적용
            boolean dfsBlockStartOffsetRangeFlag = false;
            if (fileSize > dfsBlockSize && moveToPage >= dfsBlockViewCount && !splitViewFlag) {
                dfsBlockStartOffsetRangeFlag = true;
            }

            if (dfsBlockStartOffsetRangeFlag) {
                if (buttonType.equalsIgnoreCase("nextButton")) {
                    if (moveToPage == totalPage) {
                        dfsBlockStartOffset = lastStartOffsetPerDfsBlocks.get(offsetRange);
                        chunkSizeToView = lastChunkSizePerDfsBlocks.get(offsetRange);
                    } else {
                        /**
                         * 두 번째 DFS Block 부터는 startOffset 값이 누적되어 시작됨
                         * ex) DFS Block Size : 128 MB
                         * Second DFS Block StartOffset : 2272
                         *
                         * 이동할 다음 페이지가 몇번째 DFS Block에 속해 있는지 찾아야 startOffset 값의 위치를 알 수 있다.
                         * moveToPage range per DFS block
                         *     0 ~ 13421 : First DFS Block
                         * 13422 ~ 26843
                         * 26844 ~ 53687
                         */
                        if (currentContentsBlockSize < 2) {
                            dfsBlockStartOffset += chunkSizeToView;
                        }
                    }
                } else if (buttonType.equalsIgnoreCase("prevButton")) {
                    // 현재 페이지의 내용이 두 개의 DFS Block 범위로 구성된 상태에서 이전 페이지로 이동할 때
                    if (currentPageSplitViewFlag) {
                        dfsBlockStartOffset = lastStartOffsetPerDfsBlocks.get(offsetRange - 1);
                        dfsBlockStartOffset -= chunkSizeToView;
                    } else {
                        dfsBlockStartOffset -= chunkSizeToView;
                    }
                } else if (buttonType.equalsIgnoreCase("customPage")) { // DFS Block Size가 변경되는 페이지는 splitView 플래그로 따로 처리함.
                    if (moveToPage == totalPage) {
                        dfsBlockStartOffset = lastStartOffsetPerDfsBlocks.get(offsetRange);
                        chunkSizeToView = lastChunkSizePerDfsBlocks.get(offsetRange);
                    } else {
                        long dfsBlockAccumulatedStartOffset = startOffsetPerDfsBlocks.get(offsetRange);
                        long pageCheckPoint = pageCheckPoints.get(offsetRange - 1);
                        long currentPageCount = moveToPage - pageCheckPoint;// 50000-40265=9735

                        // 이동할 페이지가 DFS Block 사이즈가 변경된 후 바로 다음 페이지일 때
                        if (currentPageCount == 1) {
                            dfsBlockStartOffset = dfsBlockAccumulatedStartOffset;
                        } else {
                            long pageRange = chunkSizeToView;
                            currentPageCount--;
                            if (currentPageCount > 0) {
                                pageRange *= currentPageCount; //97340000, 134210000
                            }
                            dfsBlockStartOffset = pageRange + dfsBlockAccumulatedStartOffset; // 97346816
                        }
                    }
                } else if (buttonType.equalsIgnoreCase("lastButton")) {
                    dfsBlockStartOffset = lastStartOffsetPerDfsBlocks.get(offsetRange);
                    chunkSizeToView = lastChunkSizePerDfsBlocks.get(offsetRange);
                }
                contentsMap.put("dfsBlockStartOffset", dfsBlockStartOffset);
            }

            contentsMap.put("currentContentsBlockSize", nextContentsBlockSize);
            contentsMap.put("offsetRange", offsetRange);

            if (fileSize < dfsBlockSize) {
                if (moveToPage == totalPage) {
                    chunkSizeToView = lastPageChunkSizeToView;
                }
            }

            /**
             * Case 1. BestNode 정보가 존재하고, Block ID가 변경되지 않았다면 해당 URL로 바로 연결한다.
             * Case 2. DataNode에서 BestNode를 선택한다.
             */
            InetSocketAddress address;
            InetSocketAddress nextAddress = null;
            DatanodeInfo chosenNode;
            DatanodeInfo nextChosenNode;

            if (contentsMap.containsKey("bestNode")
                    && !splitViewFlag
                    && !currentPageSplitViewFlag
                    && !dfsBlockStartOffsetRangeFlag
                    && !buttonType.equalsIgnoreCase("customPage")) {
                String bestNode = (String) contentsMap.get("bestNode");
                address = NetUtils.createSocketAddr(bestNode);
                contentsMap.put("bestNode", bestNode);
            } else {
                chosenNode = bestNode(locatedBlock);
                address = NetUtils.createSocketAddr(chosenNode.getName());
                contentsMap.put("bestNode", chosenNode.getName());
                if (splitViewFlag) {
                    nextChosenNode = bestNode(nextLocatedBlock);
                    nextAddress = NetUtils.createSocketAddr(nextChosenNode.getName());
                    contentsMap.put("bestNode", nextChosenNode.getName());
                }
            }

            /**
             * DFS File Block Size in HDFS
             *
             * 특정 파일의 경우 DFS 블럭 사이즈를 오버라이딩해서 HDFS에 저장하는 경우가 있기 때문에
             * 파일이 위치한 각 블럭의 사이즈를 가져온다.
             * 파일 사이즈가 클 경우 locatedBlockCount 변수값이 1보다 큼.
             *
             * 기본 설정값에 따른 DFS Block Size 정보
             * 64 (MB) >> 67,108,864 (B)
             * 128 (MB) >> 134,217,728 (B)
             */

            String poolId = locatedBlock.getBlock().getBlockPoolId();
            long blockId = locatedBlock.getBlock().getBlockId();
            long genStamp = locatedBlock.getBlock().getGenerationStamp();

            Token<BlockTokenIdentifier> blockToken = locatedBlock.getBlockToken();
            DatanodeID datanodeID = new DatanodeID(
                    address.getAddress().getHostAddress(),
                    address.getHostName(),
                    poolId,
                    address.getPort(),
                    0, 0, 0);
            Peer peer = dfsClient.newConnectedPeer(address, blockToken, datanodeID);
            CachingStrategy cachingStrategy = dfsClient.getDefaultReadCachingStrategy();
            ExtendedBlock extendedBlock = new ExtendedBlock(poolId, blockId, fileSize, genStamp);

            String contents;

            if (splitViewFlag) {
                String currentBlockContents = streamBlockInAscii(
                        address,
                        blockToken,
                        fileSize,
                        currentBlockLastStartOffset,
                        currentBlockLastChunkSizeToView,
                        fs.getConf(),
                        filePath,
                        dfsClient.getClientName(),
                        extendedBlock,
                        false,
                        peer,
                        datanodeID,
                        cachingStrategy);

                long nextBlockId = nextLocatedBlock.getBlock().getBlockId();
                long nextGenStamp = nextLocatedBlock.getBlock().getGenerationStamp();

                Token<BlockTokenIdentifier> nextBlockToken = nextLocatedBlock.getBlockToken();
                DatanodeID nextDatanodeID = new DatanodeID(
                        nextAddress.getAddress().getHostAddress(),
                        nextAddress.getHostName(),
                        poolId,
                        nextAddress.getPort(),
                        0, 0, 0);
                Peer nextPeer = dfsClient.newConnectedPeer(nextAddress, nextBlockToken, nextDatanodeID);
                CachingStrategy nextCachingStrategy = dfsClient.getDefaultReadCachingStrategy();
                ExtendedBlock nextExtendedBlock = new ExtendedBlock(poolId, nextBlockId, fileSize, nextGenStamp);

                String nextBlockContents = streamBlockInAscii(
                        nextAddress,
                        nextBlockToken,
                        fileSize,
                        nextBlockFirstStartOffset,
                        nextBlockFirstChunkSizeToView,
                        fs.getConf(),
                        filePath,
                        dfsClient.getClientName(),
                        nextExtendedBlock,
                        false,
                        nextPeer,
                        nextDatanodeID,
                        nextCachingStrategy);

                // Merge two block's contents
                contents = currentBlockContents + nextBlockContents;

                contentsMap.put("startOffset", startOffset);
            } else {
                startOffset = dfsBlockStartOffsetRangeFlag || currentPageSplitViewFlag ? dfsBlockStartOffset : startOffset;

                contents = streamBlockInAscii(
                        address,
                        blockToken,
                        fileSize,
                        startOffset,
                        chunkSizeToView,
                        fs.getConf(),
                        filePath,
                        dfsClient.getClientName(),
                        extendedBlock,
                        false,
                        peer,
                        datanodeID,
                        cachingStrategy);
            }

            contentsMap.put("chunkSizeToView", chunkSizeToView);
            contentsMap.put("lastPageChunkSizeToView", lastPageChunkSizeToView);
            contentsMap.put("contents", contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentsMap;
    }

    /**
     * 파일이 저장된 데이터노드 상태를 확인 후 최적의 노드를 선택한다.
     *
     * @param blk LocatedBlock
     * @return 데이터노드 정보
     */
    public static DatanodeInfo bestNode(LocatedBlock blk) {
        TreeSet<DatanodeInfo> deadNodes = new TreeSet<>();
        DatanodeInfo chosenNode = null;
        int failures = 0;
        Socket socket = null;
        DatanodeInfo[] nodes = blk.getLocations();

        if (nodes == null || nodes.length == 0) {
            throw new ServiceException("No nodes contain this block");
        }

        while (socket == null) {
            if (chosenNode == null) {
                do {
                    chosenNode = nodes[rand.nextInt(nodes.length)];
                } while (deadNodes.contains(chosenNode));
            }
            int index = rand.nextInt(nodes.length);
            chosenNode = nodes[index];

            //just ping to check whether the node is alive
            InetSocketAddress address = NetUtils.createSocketAddr(chosenNode.getIpAddr() + ":" + chosenNode.getInfoPort());

            try {
                socket = new Socket();
                socket.connect(address, HdfsServerConstants.READ_TIMEOUT);
                socket.setSoTimeout(HdfsServerConstants.READ_TIMEOUT);
            } catch (IOException e) {
                deadNodes.add(chosenNode);
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                socket = null;
                failures++;
            }

            if (failures == nodes.length) {
                throw new ServiceException("Could not reach the block containing the data. Please try again");
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chosenNode;
    }

    /**
     * 선택된 최적 노드에서 스트림을 읽어들인다.
     *
     * @param inetSocketAddress     InetSocketAddress
     * @param blockToken            Token<BlockTokenIdentifier>
     * @param fileSize              FileSize
     * @param startOffset           StartOffset
     * @param chunkSizeToView       ChunkSizeToView
     * @param conf                  Configuration
     * @param fileLocation          FileLocation
     * @param clientName            ClientName
     * @param block                 Block
     * @param verifyChecksum        VerifyChecksum
     * @param peer                  Peer
     * @param datanodeID            DataNodeID
     * @param cachingStrategy       CachingStrategy
     * @return String
     * @throws IOException
     */
    public String streamBlockInAscii(InetSocketAddress inetSocketAddress,
                                     Token<BlockTokenIdentifier> blockToken,
                                     long fileSize,
                                     long startOffset,
                                     long chunkSizeToView,
                                     Configuration conf,
                                     String fileLocation,
                                     String clientName,
                                     ExtendedBlock block,
                                     boolean verifyChecksum,
                                     Peer peer,
                                     DatanodeID datanodeID,
                                     CachingStrategy cachingStrategy) throws IOException {
        if (chunkSizeToView == 0) {
            throw new ServiceException("Cannot read chunk size to view.");
        }

        Socket socket = NetUtils.getDefaultSocketFactory(conf).createSocket();
        socket.connect(inetSocketAddress, HdfsServerConstants.READ_TIMEOUT);
        socket.setSoTimeout(HdfsServerConstants.READ_TIMEOUT);

        BlockReader blockReader =
                RemoteBlockReader2.newBlockReader(
                        fileLocation,
                        block,
                        blockToken,
                        startOffset,
                        chunkSizeToView,
                        verifyChecksum,
                        clientName,
                        peer,
                        datanodeID,
                        null,
                        cachingStrategy);

        int amtToRead = (int) Math.min(chunkSizeToView, fileSize);
        final byte[] buf = new byte[amtToRead];
        int readOffset = 0;
        int retires = 2;

        while (amtToRead > 0) {
            int numRead = amtToRead;
            try {
                blockReader.readFully(buf, readOffset, amtToRead);
            } catch (IOException e) {
                retires--;
                if (retires == 0) {
                    throw new ServiceException("Could not read data from datanode.");
                }
                continue;
            }
            amtToRead -= numRead;
            readOffset += numRead;
        }

        blockReader.close();
        socket.close();
        return new String(buf);
    }

    /**
     * 지정한 경로에 사용자 및 그룹, 권한을 변경한다.
     *
     * @param permissionMap 변경할 권한 정보
     * @return true or false
     */
    public boolean setPermission(Map permissionMap) {
        String srcPath = (String) permissionMap.get("currentPath");
        String owner = (String) permissionMap.get("owner");
        String group = (String) permissionMap.get("group");
        String fileStatus = (String) permissionMap.get("fileStatus");
        boolean recursiveOwner = (int) permissionMap.get("recursiveOwner") == 1;
        boolean recursivePermission = (int) permissionMap.get("recursivePermission") == 1;
        String permission = (String) permissionMap.get("permission");

        try {
            if (srcPath.equalsIgnoreCase("/")) {
                throw new ServiceException("루트는 권한을 변경할 수 없습니다.");
            }

            if (fileStatus.equalsIgnoreCase("DIRECTORY")) {
                runChown(recursiveOwner, owner, group, srcPath);
                runChmod(recursivePermission, permission, srcPath);
            } else {
                FileSystem fs = FileSystem.get(Namenode2Agent.configuration);
                FsPermission fsPermission = new FsPermission(permission);
                String file = (String) permissionMap.get("file");

                if (file.equalsIgnoreCase("/")) {
                    throw new ServiceException("루트는 권한을 변경할 수 없습니다.");
                }

                fs.setOwner(new Path(file), owner, group);
                fs.setPermission(new Path(file), fsPermission);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServiceException("권한을 변경할 수 없습니다.", ex);
        }

        return true;
    }

    /**
     * HDFS 경로에 사용자 홈 디렉토리를 생성한다.
     *
     * @param hdfsUserMap HDFS 사용자 홈 디렉토리 생성 및 권한 설정에 필요한 정보
     * @return true or false
     */
    public boolean createUserHome(Map hdfsUserMap) {
        String hdfsUserHome = (String) hdfsUserMap.get("hdfsUserHome");
        String username = (String) hdfsUserMap.get("username");
        Path path = new Path(hdfsUserHome);

        if (!FileUtils.pathValidator(hdfsUserHome)) {
            throw new ServiceException("Invalid path. Please check the path.");
        }

        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);

            if (!fs.mkdirs(path)) {
                throw new ServiceException("사용자 홈 디렉토리를 생성할 수 없습니다.");
            }

            fs.setOwner(path, username, username);
            fs.setPermission(path, new FsPermission((short) 00700));
        } catch (Exception ex) {
            throw new ServiceException("사용자 홈 디렉토리 권한을 설정할 수 없습니다.");
        }

        return true;
    }

    /**
     * HDFS 경로에 존재하는 사용자 홈 디렉토리를 삭제한다.
     *
     * @param hdfsUserHomePath HDFS 사용자 홈 디렉토리 경로
     * @return true or false
     */
    public boolean deleteUserHome(String hdfsUserHomePath) {
        boolean deleted;

        if (!FileUtils.pathValidator(hdfsUserHomePath)) {
            throw new ServiceException("Invalid path. Please check the path.");
        }

        if (!exists(hdfsUserHomePath)) {
            throw new ServiceException("사용자 홈 디렉토리가 존재하지 않습니다.");
        }

        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);
            Path path = new Path(hdfsUserHomePath);

            logger.debug("Delete HDFS User Home Path : {}", path);

            deleted = fs.delete(path, true);

            if (!deleted) {
                exceptionLogger.warn("Failed delete HDFS User Home : {}", path);
                throw new ServiceException("사용자 홈 디렉토리를 삭제할 수 없습니다.");
            }

            return true;
        } catch (Exception ex) {
            throw new ServiceException("사용자 홈 디렉토리를 삭제할 수 없습니다.");
        }
    }

    /**
     * 선택한 HDFS 경로의 권한(소유권)을 변경한다.
     *
     * @param recursive 하위 경로 포함 옵션
     * @param owner     소유자
     * @param group     그룹
     * @param srcPath   권한을 변경할 HDFS 경로
     * @return true or false
     */
    private boolean runChown(boolean recursive, String owner, String group, String srcPath) {
        try {
            FsShell fsShell = new FsShell(Namenode2Agent.configuration);
            String ownership = MessageFormatter.arrayFormat("{}:{}", new String[]{owner, group}).getMessage();
            int result;

            if (recursive) {
                result = fsShell.run(new String[]{"-chown", "-R", ownership, srcPath});
            } else {
                result = fsShell.run(new String[]{"-chown", ownership, srcPath});
            }

            logger.debug("선택한 HDFS 경로 '{}'의 소유권이 변경되었습니다[Recursive : {}].", srcPath, recursive);

            return result == 0;
        } catch (Exception ex) {
            logger.warn("해당 경로'{}'가 HDFS 파일시스템에 존재하지 않거나 확인할 수 없습니다.", srcPath);
            exceptionLogger.warn("{} : {}\n{}", new String[]{
                    ex.getClass().getName(), ex.getMessage(), ExceptionUtils.getFullStackTrace(ex)
            });
            return false;
        }
    }

    /**
     * 선택한 HDFS 경로의 권한(허가권)을 변경한다.
     *
     * @param recursive  하위 경로 포함 옵션
     * @param permission 적용할 권한 정보 (ex. 777)
     * @param srcPath    권한을 변경할 HDFS 경로
     * @return true of false
     */
    private boolean runChmod(boolean recursive, String permission, String srcPath) {
        try {
            FsShell fsShell = new FsShell(Namenode2Agent.configuration);
            int result;

            if (recursive) {
                result = fsShell.run(new String[]{"-chmod", "-R", permission, srcPath});
            } else {
                result = fsShell.run(new String[]{"-chmod", permission, srcPath});
            }

            logger.debug("선택한 HDFS 경로 '{}'의 파일 접근 권한이 변경되었습니다[Recursive : {}].", srcPath, recursive);

            return result == 0;
        } catch (Exception ex) {
            logger.warn("해당 경로'{}'가 HDFS 파일시스템에 존재하지 않거나 확인할 수 없습니다.", srcPath);
            exceptionLogger.warn("{} : {}\n{}", new String[]{
                    ex.getClass().getName(), ex.getMessage(), ExceptionUtils.getFullStackTrace(ex)
            });
            return false;
        }
    }

    /**
     * 해당 경로에 디렉토리 및 파일이 존재하는지 확인한다.
     *
     * @param path HDFS의 파일 또는 디렉토리
     * @return true or false
     */
    public boolean exists(String path) {
        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);

            return fs.exists(new Path(path));
        } catch (Exception ex) {
            throw new ServiceException("지정한 경로가 존재하는지 확인할 수 없습니다.", ex);
        }
    }

    /**
     * HDFS 파일 시스템에서 선택한 디렉토리 및 파일을 리눅스 사용자 홈 디렉토리로 복사한다.
     *
     * @param srcFullyQualifiedPath     복사할 디렉토리 및 파일의 HDFS 경로
     * @param linuxUserHome             복사될 리눅스 사용자 홈 경로
     */
    public boolean copyToLocal(String srcFullyQualifiedPath, String dstFullyQualifiedPath, String linuxUserHome, String username) {
        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);
            Path path = new Path(srcFullyQualifiedPath);
            Path localPath = new Path(dstFullyQualifiedPath);
            String mode = "666"; //rw-rw-rw-

            Configuration conf = new Configuration();
            LocalFileSystem lfs = (LocalFileSystem) LocalFileSystem.get(URI.create("file:///"), conf);

            if (lfs.createNewFile(localPath)) {
                fs.copyToLocalFile(false, path, new Path(linuxUserHome), false);
                lfs.setPermission(localPath, new FsPermission(mode));
            } else {
                return false;
            }

            return true;
        } catch (IOException ex) {
            throw new ServiceException("지정한 경로가 존재하는지 확인할 수 없습니다.", ex);
        }
    }

    /**
     * 선택한 경로의 디렉토리 또는 파일의 소유권ㅇ르 접속한 사용자의 소유권으로 변경한다.
     *
     * @param path      HDFS의 디렉토리 또는 파일
     * @param username  Username
     * @param userGroup UserGroup
     */
    private void setOwnership(Path path, String username, String userGroup) {
        try {
            FileSystem fs = FileSystem.get(Namenode2Agent.configuration);

            fs.setOwner(path, username, userGroup);
        } catch (IOException ex) {
            throw new ServiceException("소유권을 변경할 수 없습니다.", ex);
        }
    }
}