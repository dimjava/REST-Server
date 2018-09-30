package com.reshigo.dao;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileShare;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Created by dmitry103 on 03/02/17.
 */

@Service
public class FileUtil {
    @Autowired
    @Qualifier("commonProperties")
    private Properties commonProperties;

    @Autowired
    private CloudFileShare cloudFileShare;

    public byte[] getImage(String path) throws IOException, URISyntaxException, StorageException {
        //return IOUtils.toByteArray(new FileInputStream(commonProperties.getProperty("data.path") + path));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        cloudFileShare.getRootDirectoryReference().getFileReference(path).download(os);

        return os.toByteArray();
    }

    public void deleteImage(String path) {
        //this causes microsoft remote disk umount
        //File f = new File(commonProperties.getProperty("data.path") + path);

        //f.delete();
        //f.getParentFile().delete();
        //f.getParentFile().getParentFile().delete();

        CloudFile dir;
        try {
            dir = cloudFileShare.getRootDirectoryReference().getFileReference(path);
            dir.deleteIfExists();
            dir.getParent().deleteIfExists();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String saveOrdersImage(Long orderId, Long pictureId, byte[] data) throws IOException, URISyntaxException, StorageException {
        return saveImage(orderId, pictureId, data, "orders");
    }

    public String saveMessageImage(Long chatId, Long messageId, byte[] data) throws IOException, URISyntaxException, StorageException {
        return saveImage(chatId, messageId, data, "chats");
    }

    public String saveProfileImage(String userName, byte[] data) throws IOException, URISyntaxException, StorageException {
//        File f = new File(commonProperties.getProperty("data.path") + "profiles/" + userName + "/photo.jpg");
//        f.getParentFile().mkdirs();
//        f.createNewFile();

//        FileOutputStream os = new FileOutputStream(f);
//        os.write(data);

        String path = "profiles/" + userName + "/photo.jpg";
        cloudFileShare.getRootDirectoryReference().getDirectoryReference("profiles/" + userName).createIfNotExists();
        cloudFileShare.getRootDirectoryReference().getFileReference(path).uploadFromByteArray(data, 0, data.length);

        return "profiles/" + userName + "/photo.jpg";
    }

    private String saveImage(Long id1, Long id2, byte[] data, String prefix) throws IOException, URISyntaxException, StorageException {
//        File f = new File(commonProperties.getProperty("data.path") + prefix + "/" + id1 + "/" + id2 + ".jpg");
//        f.getParentFile().mkdirs();
//        f.createNewFile();
//
//        FileOutputStream os = new FileOutputStream(f);
//        os.write(data);

        String path = prefix + "/" + id1 + "/" + id2 + ".jpg";
        cloudFileShare.getRootDirectoryReference().getDirectoryReference(prefix + "/" + id1).createIfNotExists();
        cloudFileShare.getRootDirectoryReference().getFileReference(path).uploadFromByteArray(data, 0, data.length);

        return prefix + "/" + id1 + "/" + id2 + ".jpg";
    }
}
