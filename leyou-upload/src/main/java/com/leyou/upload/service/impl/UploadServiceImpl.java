package com.leyou.upload.service.impl;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.upload.service.UploadService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadServiceImpl implements UploadService {

    private static final List<String> CONTEXT_TYPES = Arrays.asList("image/gif","image/jpeg");
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UploadService.class);

    @Autowired
    private FastFileStorageClient storageClient;

    /**
     * 上传文件并返回上传地址
     * @param file
     * @return
     */
    @Override
    public String uploadImage(MultipartFile file) {
        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        if (!CONTEXT_TYPES.contains(contentType)) {
            LOGGER.info("需要图片文件类型，当前文件类型不合法，文件名：{}", originalFilename);
            return null;
        }
        try {
            // 校验文件的内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                LOGGER.info("当前文件内容不合法，文件名：{}", originalFilename);
            }
            // 保存到文件服务器
            String ext = StringUtils.substringAfterLast(originalFilename, ".");
            StorePath storePath = this.storageClient.uploadFile(file.getInputStream(), file.getSize(), ext, null);
            //file.transferTo(new File("D:\\IdeaProjects\\pingyougou\\images\\" + originalFilename));
            // 返回url，进行回显
            return "http://image.leyou.com/" + storePath.getFullPath();
        } catch (IOException e) {
            LOGGER.info("服务器内部错误 {}", originalFilename);
            e.printStackTrace();
        }
        return null;
    }
}
