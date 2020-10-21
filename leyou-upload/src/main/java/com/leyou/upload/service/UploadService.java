package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {

    private static final List<String> CONTENT_TYPES= Arrays.asList("image/jepg","image/gif");

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);

    @Autowired
    private FastFileStorageClient storageClient;

    public String upload(MultipartFile file){

        String originalFilename = file.getOriginalFilename();
        //1.检校文件类型
        String contentType = file.getContentType();
        if (!CONTENT_TYPES.contains(contentType)){
            //文件类型不合法
            LOGGER.info("文件上传失败: {},文件类型不合法", originalFilename);
            return null;
        }

        try{
            //2.检校文件内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage== null){
                LOGGER.info("文件上传失败: {},文件不存在", originalFilename);
                return null;
            }
            //3. 保存到服务器
            String ext = StringUtils.substringAfterLast(originalFilename,".");
            StorePath storePath = this.storageClient.uploadFile(file.getInputStream(),file.getSize(),ext, null);

            //4. 生产url地址,返回
            return  "http://image.leyou.com/"+storePath.getFullPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
