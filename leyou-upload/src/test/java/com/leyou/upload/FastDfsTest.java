package com.leyou.upload;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FastDfsTest {
    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private ThumbImageConfig thumbImageConfig;

    @Test
    public void testUpload() throws FileNotFoundException {
        // 要上传的文件
        File file = new File("D:\\IdeaProjects\\pingyougou\\images\\user5-128x128.jpg");
        // 上传并保存图片，参数：1-上传的文件流 2-文件的大小 3-文件的后缀 4-可以不管他
        StorePath storePath = this.storageClient.uploadFile(
                new FileInputStream(file), file.length(), "jpg", null);
        // 带分组的路径   group1/M00/00/00/wKgKhF-ultSAHg_PAAAZLmoT79w755.jpg
        System.out.println(storePath.getFullPath());
        // 不带分组的路径  M00/00/00/wKgKhF-ultSAHg_PAAAZLmoT79w755.jpg
        System.out.println(storePath.getPath());
    }

    @Test
    public void testUploadAndCreateThumb() throws FileNotFoundException {
        File file = new File("D:\\IdeaProjects\\pingyougou\\images\\user5-128x128.jpg");
        // 上传并且生成缩略图
        StorePath storePath = this.storageClient.uploadImageAndCrtThumbImage(
                new FileInputStream(file), file.length(), "jpg", null);
        // 带分组的路径   group1/M00/00/00/wKgKhF-umKeAMY60AAAZLmoT79w096.jpg
        System.out.println(storePath.getFullPath());
        // 不带分组的路径   M00/00/00/wKgKhF-umKeAMY60AAAZLmoT79w096.jpg
        System.out.println(storePath.getPath());
        // 获取缩略图路径    M00/00/00/wKgKhF-umKeAMY60AAAZLmoT79w096_60x60.jpg
        String path = thumbImageConfig.getThumbImagePath(storePath.getPath());
        System.out.println(path);
    }
}
