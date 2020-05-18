package com.fitsoft.config;

import com.fitsoft.utils.PropertyUtil;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.exception.FdfsServerException;
import com.github.tobato.fastdfs.exception.FdfsUnsupportStorePathException;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
@Component
public class FastDFSClient {

	@Resource
	private FastFileStorageClient storageClient;

	@Resource
	private PropertyUtil propertyUtil;

	/**
	 * 上传文件
	 * @param file 文件对象
	 * @param flag 是否获取完整地址，true 完整
	 * @return 文件访问地址
	 * @throws IOException IO异常
	 */
	public String uploadFile(MultipartFile file, boolean flag) throws IOException {
		StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(),
				FilenameUtils.getExtension(file.getOriginalFilename()), null);
		return flag?getResAccessUrl(storePath.getFullPath()):storePath.getFullPath();
	}
	
	/**
	 * 将一段字符串生成一个文件上传
	 * @param content 文件内容
	 * @param fileExtension 文件后缀
	 * @param flag 是否获取完整地址，true 完整
	 * @return 文件路径
	 */
	public String uploadFile(String content, String fileExtension, boolean flag) {
		byte[] buff = content.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream stream = new ByteArrayInputStream(buff);
		StorePath storePath = storageClient.uploadFile(stream, buff.length, fileExtension, null);
		return flag?getResAccessUrl(storePath.getFullPath()):storePath.getFullPath();
	}

	/**
	 * 删除文件
	 * @param fileUrl 文件访问地址
	 */
	public void deleteFile(String fileUrl) {
		if (StringUtils.isEmpty(fileUrl)) {
			return;
		}
		try {
			StorePath storePath = StorePath.parseFromUrl(fileUrl);
			storageClient.deleteFile(storePath.getGroup(), storePath.getPath());
		} catch (FdfsUnsupportStorePathException | FdfsServerException e) {
			e.getMessage();
		}
	}

	// 封装完整URL地址
	public String getResAccessUrl(String fullPath) {
		return "http://" +
				propertyUtil.getURL()
				+ ":" + propertyUtil.getPORT()
				+ "/" + fullPath;
	}
}
