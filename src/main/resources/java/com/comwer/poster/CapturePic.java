package com.comwer.poster;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CapturePic {
	private static Logger logger = LoggerFactory.getLogger(CapturePic.class);

	// 默认超时设置（毫秒）
	private static final int DEFAULT_TIMEOUT = 5000;

	/**
	 * 从 HttpResponse 实例中获取状态码、错误信息、以及响应信息等等.
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private static HttpEntity handleResponse(final HttpResponse response) throws IOException {
		// 状态码
		final StatusLine statusLine = response.getStatusLine();
		// 获取响应实体
		final HttpEntity entity = response.getEntity();

		// 状态码一旦大于 300 表示请求失败
		if (statusLine.getStatusCode() >= 300) {
			EntityUtils.consume(entity);
			throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
		}
		return entity;
	}

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

		// 根据路径发起 HTTP get 请求
		HttpGet httpget = new HttpGet(picURL);
		// 使用 addHeader 方法添加请求头部
		httpget.addHeader("Content-Type", "text/html;charset=UTF-8");
		httpget.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

		// 配置请求的超时设置
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(timeout)
				.setConnectTimeout(timeout)
				.setSocketTimeout(timeout)
				.build();
		httpget.setConfig(requestConfig);

		File pic = null;

		// 使用 HttpClientBuilder 创建 CloseableHttpClient 对象
		try (CloseableHttpClient httpclient = HttpClientBuilder.create().build();
			 CloseableHttpResponse response = httpclient.execute(httpget);
			 InputStream picStream = handleResponse(response).getContent()) {

			pic = createTmpFile(picStream, "pic_", ".jpg", directory);
		} catch (Exception e) {
			logger.error("下载图片失败: " + e.getMessage(), e);
			e.printStackTrace(); // 添加这行以便在控制台看到完整错误信息
			throw new RuntimeException("下载图片失败: " + e.getMessage(), e);
		} finally {
			// 释放连接
			httpget.releaseConnection();
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
