package com.comwer.poster;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

	// 默认临时目录，可以通过方法参数覆盖
//	public static final Path DEFAULT_DIRECTORY = Paths.get(System.getProperty("java.io.tmpdir"));
	public static final Path DEFAULT_DIRECTORY = Paths.get(System.getProperty("user.dir"));

	public static void main(String[] args) throws IOException {
		read("bg_", ".jpg", "default_bgimg.jpg");
		read("qrcode_", ".jpg", "default_qrcodeimg.jpg");
	}

	public static File read(String prefix, String suffix, String resource) throws IOException {
		return read(prefix, suffix, resource, DEFAULT_DIRECTORY);
	}

	public static File read(String prefix, String suffix, String resource, Path directory) throws IOException {
		// 确保目录存在
		if (!Files.exists(directory)) {
			Files.createDirectories(directory);
		}
		
		// ClassLoader.getResourceAsStream() 会从 classpath 的根路径下查找资源
		ClassLoader classLoader = FileUtil.class.getClassLoader();

		File file = Files.createTempFile(directory, prefix, suffix).toFile();
		try (InputStream inputStream = classLoader.getResourceAsStream(resource)) {
			if (inputStream == null) {
				throw new IOException("资源未找到: " + resource);
			}
			FileUtils.copyInputStreamToFile(inputStream, file);
		}
		logger.debug("资源已加载: " + file.getAbsolutePath());

		return file;
	}

}
