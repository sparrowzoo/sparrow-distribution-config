package com.sparrow.distribution.config.repository.local.file;

import com.sparrow.distribution.config.fetch.HttpDownloader;
import com.sparrow.distribution.config.protocol.Protocol;
import com.sparrow.distribution.config.repository.Repository;
import com.sparrow.support.EnvironmentSupport;
import com.sparrow.utility.FileUtility;
import com.sparrow.utility.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

public class FileRepository implements Repository {
    private String localPath;
    private HttpDownloader httpDownloader;

    private Logger logger = LoggerFactory.getLogger(FileRepository.class);

    public FileRepository(HttpDownloader httpDownloader, String localPath) {
        this.httpDownloader = httpDownloader;
        this.localPath = localPath;
    }

    @Override
    public void fetch(String fileName) {
        ByteBuffer buffer = this.httpDownloader.fetch(fileName);
        if (buffer == null) {
            return;
        }
        FileUtility.getInstance().writeFile(this.getLocalFullFileName(fileName),new String(buffer.array()));
        buffer.clear();
    }

    private String getLocalFullFileName(String fileName) {
        return StringUtility.join(File.pathSeparator, EnvironmentSupport.getInstance().getClassesPhysicPath(), this.localPath, fileName);
    }

    @Override
    public void receive(Protocol protocol) {
        String fullFileName = this.getLocalFullFileName(protocol.getFileName());
        FileUtility.getInstance().writeFile(fullFileName, protocol.getBody());
    }

    @Override
    public Properties read(String fileName) {
        String fullFileName = this.getLocalFullFileName(fileName);
        File file = new File(fullFileName);
        if (!file.exists()) {
            return null;
        }
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(file));
            return properties;
        } catch (IOException e) {
            logger.error("load file error", e);
            return null;
        }
    }
}
