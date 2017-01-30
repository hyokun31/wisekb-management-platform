package wisekb.web.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.net.URI;

/**
 * Created by sanghyunbak on 2016. 11. 28..
 */
public class HdfsUtils {
    public static void localFileToHdfs(String localFilePath, String hdfsFilePath) throws IOException {
        Configuration conf = new Configuration();
        conf.set("dfs.namenode.replication.min", "0");
        //conf.set("dfs.client.use.datanode.hostname", "true");
        FileSystem fs = FileSystem.get(URI.create(hdfsFilePath), conf);
        fs.copyFromLocalFile(new Path(localFilePath), new Path(hdfsFilePath));
    }
}
