package com.fitsoft.utils;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("unused")
@Service
public class FileUtils {
	/**
	 * 根据url拿取file
	 * @param url 文件的链接
	 * @param suffix 文件后缀名
	 */
	public static File createFileByUrl(String url, String suffix) {
		byte[] byteFile = getImageFromNetByUrl(url);
		if (byteFile != null) {
			return getFileFromBytes(byteFile, suffix);
		} else {
			return null;
		}
	}

	/**
	 * 根据地址获得数据的字节流
	 * @param strUrl 网络连接地址
	 * @return 数据的字节流
	 */
	private static byte[] getImageFromNetByUrl(String strUrl) {
		try {
			URL url = new URL(strUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5 * 1000);
			InputStream inStream = conn.getInputStream();// 通过输入流获取图片数据
			return readInputStream(inStream);// 得到图片的二进制数据
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 从输入流中获取数据
	 * @param inStream 输入流
	 * @return 数据的字节流
	 * @throws IOException IO错误
	 */
	private static byte[] readInputStream(InputStream inStream) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		inStream.close();
		return outStream.toByteArray();
	}

	// 创建临时文件
	private static File getFileFromBytes(byte[] b, String suffix) {
		BufferedOutputStream stream = null;
		File file = null;
		try {
			file = File.createTempFile("pattern", "." + suffix);
			System.out.println("临时文件位置：" + file.getCanonicalPath());
			FileOutputStream fstream = new FileOutputStream(file);
			stream = new BufferedOutputStream(fstream);
			stream.write(b);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return file;
	}

	public static MultipartFile createImg(String url) {
		try {
			// File转换成MultipartFile
			File file = FileUtils.createFileByUrl(url, "jpg");
			if(file == null)
				return null;
			FileInputStream inputStream = new FileInputStream(file);
			return new MockMultipartFile(file.getName(), inputStream);
		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static MultipartFile fileToMultipart(String filePath) {
		try {
			// File转换成MultipartFile
			File file = new File(filePath);
			FileInputStream inputStream = new FileInputStream(file);
			return new MockMultipartFile(file.getName(), "png", "image/png", inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
