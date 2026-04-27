package com.teak.core.api;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * 文件存储服务接口，用于抽象不同的存储后端（本地、OSS、S3等）。
 */
public interface FileStorageService {

    /**
     * 存储文件
     * @param file 上传的文件
     * @param key 存储键（路径）
     * @return 存储后的访问URL
     */
    String store(MultipartFile file, String key);

    /**
     * 存储字节数据
     */
    String store(byte[] data, String key);

    /**
     * 下载文件为资源
     */
    Resource loadAsResource(String key);

    /**
     * 获取文件输入流
     */
    InputStream loadAsStream(String key);

    /**
     * 删除文件
     */
    boolean delete(String key);

    /**
     * 检查文件是否存在
     */
    boolean exists(String key);

    /**
     * 获取文件URL（如果支持）
     */
    String getUrl(String key);
}