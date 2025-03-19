package com.lhk.poster;

import cn.hutool.core.io.IoUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class CapturePic {

	private static final Logger logger = LoggerFactory.getLogger(CapturePic.class);

	// 默认超时设置（毫秒）
	private static final int DEFAULT_TIMEOUT = 5000;

	/**
	 * 将输入流信息读入到临时文件中
	 * 
	 * @param inputStream
	 * @param prefix
	 * @param suffix
	 * @param directory
	 * @return
	 * @throws IOException
	 */
	private static File createTmpFile(InputStream inputStream, String prefix, String suffix, Path directory) throws IOException {
		// 确保目录存在
		if (!Files.exists(directory)) {
			Files.createDirectories(directory);
		}
		
		// 在指定目录中创建一个新的空文件，使用给定的前缀和后缀字符串生成其名称
		File tmpFile = Files.createTempFile(directory, prefix, suffix).toFile();

		// 新建文件输出流对象
		try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
			int read = 0;
			byte[] bytes = new byte[1024 * 100];
			while ((read = inputStream.read(bytes)) != -1) {
				fos.write(bytes, 0, read);
			}
			fos.flush();
		}
		
		return tmpFile;
	}

	/**
	 * 从URL下载图片
	 * 
	 * @param picURL 图片URL
	 * @param directory 保存目录
	 * @param timeout 超时时间（毫秒）
	 * @return 下载的图片文件
	 */
	public static File capture(String picURL, Path directory, int timeout) {
		if (StringUtils.isEmpty(picURL)) {
			logger.error("图片URL不能为空");
			throw new IllegalArgumentException("图片URL不能为空");
		}
		
		if (directory == null) {
			directory = FileUtil.DEFAULT_DIRECTORY;
		}
		
		if (timeout <= 0) {
			timeout = DEFAULT_TIMEOUT;
		}

		logger.debug("从URL下载图片: " + picURL);

		File pic = null;
		HttpResponse response = null;

		try {
			// 使用Hutool发送HTTP请求，并设置自动重定向
			response = HttpRequest.get(picURL)
					.header("Content-Type", "text/html;charset=UTF-8")
					.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
					.timeout(timeout)
					.setFollowRedirects(true)
					.execute();
			
			// 检查响应状态码
			if (response.getStatus() >= 400) {
				throw new HttpException("HTTP请求失败，状态码: " + response.getStatus());
			}
			
			// 获取响应输入流并创建临时文件
			try (InputStream picStream = response.bodyStream()) {
				pic = createTmpFile(picStream, "pic_", ".jpg", directory);
			}
		} catch (Exception e) {
			logger.error("下载图片失败: " + e.getMessage(), e);
			e.printStackTrace(); // 添加这行以便在控制台看到完整错误信息
			throw new RuntimeException("下载图片失败: " + e.getMessage(), e);
		} finally {
			// 关闭响应
			if (response != null) {
				IoUtil.close(response);
			}
		}

		if (pic == null || !pic.exists()) {
			logger.error("图片下载失败，请提供正确的网络图片路径！");
			throw new IllegalArgumentException("请提供正确的网络图片路径！");
		}
		
		logger.debug("图片已下载到: " + pic.getAbsolutePath());
		return pic;
	}

	/**
	 * 使用默认参数从URL下载图片
	 */
	public static File capture(String picURL) {
		return capture(picURL, FileUtil.DEFAULT_DIRECTORY, DEFAULT_TIMEOUT);
	}

	/**
	 * 使用默认参数从金山词霸获取每日图片
	 * 此方法仅用于测试和兼容旧代码
	 */
	public static File capture() throws IOException {
		// 使用一个默认的图片URL
		String defaultImageUrl = "https://picsum.photos/800/600"; // 使用随机图片服务
		return capture(defaultImageUrl);
	}
}
