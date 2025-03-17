package com.comwer.poster;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 海报生成器示例类
 * 展示如何以编程方式使用海报生成功能
 */
public class PosterGenerator {
    private static Logger logger = LoggerFactory.getLogger(PosterGenerator.class);

    public static void main(String[] args) {
        // 示例1：从网络图片生成海报
        generateFromUrl();
        
        // 示例2：从本地图片生成海报
//        generateFromLocalImage();
        
        // 示例3：使用自定义参数生成海报
//        generateWithCustomParams();
    }
    
    /**
     * 示例1：从网络图片生成海报
     */
    private static void generateFromUrl() {
        try {
            // 使用更可靠的图片URL
            String imageUrl = "https://picsum.photos/800/600"; // 使用随机图片服务
            
            // 中文内容
            String zhText = "编程改变世界，创新引领未来。";
            
            // 英文内容
            String enText = "Programming changes the world, innovation leads the future.";
            
            // 生成海报
            String posterPath = PosterUtil.create(imageUrl, zhText, enText);

            System.out.println("posterPath = " + posterPath);
            logger.info("海报已生成: " + posterPath);
        } catch (Exception e) {
            logger.error("生成海报失败: " + e.getMessage(), e);
            e.printStackTrace(); // 添加这行以便在控制台看到完整错误信息
        }
    }
    
    /**
     * 示例2：从本地图片生成海报
     */
    private static void generateFromLocalImage() {
        try {
            // 本地图片路径
            String imagePath = "D:/images/sample.jpg";
            
            // 中文内容
            String zhText = "技术成就梦想，代码书写人生。";
            
            // 英文内容
            String enText = "Technology achieves dreams, code writes life.";
            
            // 输出目录
            Path outputDir = Paths.get("D:/posters");
            
            // 生成海报
            String posterPath = PosterUtil.createFromLocalImage(imagePath, zhText, enText, outputDir);
            
            logger.info("海报已生成: " + posterPath);
        } catch (Exception e) {
            logger.error("生成海报失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 示例3：使用自定义参数生成海报
     */
    private static void generateWithCustomParams() {
        try {
            // 网络图片URL
            String imageUrl = "https://example.com/another-image.jpg";
            
            // 中文内容
            String zhText = "探索无限可能，创造美好未来。";
            
            // 英文内容
            String enText = "Explore infinite possibilities, create a better future.";
            
            // 自定义背景图片
            String bgImagePath = "D:/images/custom-background.jpg";
            
            // 自定义二维码图片
            String qrcodePath = "D:/images/custom-qrcode.jpg";
            
            // 输出目录
            Path outputDir = Paths.get("D:/custom-posters");
            
            // 生成海报
            String posterPath = PosterUtil.create(imageUrl, zhText, enText, bgImagePath, qrcodePath, outputDir);
            
            logger.info("自定义海报已生成: " + posterPath);
        } catch (Exception e) {
            logger.error("生成自定义海报失败: " + e.getMessage(), e);
        }
    }
} 