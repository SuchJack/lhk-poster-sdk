package com.lhk.poster;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.font.FontDesignMetrics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class PosterUtil {

    private static final Logger logger = LoggerFactory.getLogger(PosterUtil.class);

    /**
     * 留白
     */
    private static final int MARGIN = 25;
    /**
     * 使用的字体
     */
    private static final String USE_FONT_NAME = "微软雅黑";

    public static void drawQrcode(Graphics2DPoster graphics2dPoster) {
        BufferedImage qrcodeImage = graphics2dPoster.getQrcodeImage();
        BufferedImage bgImage = graphics2dPoster.getBgImage();

        // 二维码起始坐标
        int qrcode_x = bgImage.getWidth() - qrcodeImage.getWidth() - MARGIN;
        int qrcode_y = bgImage.getHeight() - qrcodeImage.getHeight() - MARGIN;

        Graphics2D graphics2d = graphics2dPoster.getGraphics2d();
        graphics2d.drawImage(qrcodeImage, qrcode_x, qrcode_y, qrcodeImage.getWidth(), qrcodeImage.getHeight(), null);

        // 追加二维码描述文本
        graphics2d.setColor(new Color(71, 71, 71));
        Font font = new Font(USE_FONT_NAME, Font.PLAIN, 22);
        graphics2d.setFont(font);
        FontDesignMetrics metrics = FontDesignMetrics.getMetrics(graphics2d.getFont());

        graphics2d.drawString("SuchJack", MARGIN, bgImage.getHeight() - MARGIN - metrics.getHeight() * 2);
        graphics2d.drawString("一个幽默的程序员", MARGIN, bgImage.getHeight() - MARGIN - metrics.getDescent());
    }

    public static void drawEnString(Graphics2DPoster graphics2dPoster) throws IOException {
        // 设置封面图和下方中文之间的距离
        graphics2dPoster.addCurrentY(20);

        Graphics2D graphics2d = graphics2dPoster.getGraphics2d();
        graphics2d.setColor(new Color(157, 157, 157));

        FontDesignMetrics metrics = FontDesignMetrics.getMetrics(graphics2d.getFont());
        String enWrap = FontUtil.makeEnLineFeed(graphics2dPoster.getEn(), metrics, graphics2dPoster.getSuitableWidth());
        String[] enWraps = enWrap.split("\n");
        for (int i = 0; i < enWraps.length; i++) {
            graphics2dPoster.addCurrentY(metrics.getHeight());
            graphics2d.drawString(enWraps[i], MARGIN, graphics2dPoster.getCurrentY());
        }
    }

    public static Graphics2DPoster drawZhString(Graphics2DPoster graphics2dPoster) {
        // 获取计算机上允许使用的中文字体
        List<String> fontNames = Arrays
                .asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        if (fontNames == null || !fontNames.contains(USE_FONT_NAME)) {
            throw new RuntimeException("计算机上未安装" + USE_FONT_NAME + "的字体");
        }

        // 设置封面图和下方中文之间的距离
        graphics2dPoster.addCurrentY(30);

        Graphics2D graphics2d = graphics2dPoster.getGraphics2d();
        graphics2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Font 的构造参数依次是字体名字，字体式样，字体大小
        Font font = new Font(USE_FONT_NAME, Font.PLAIN, 28);
        graphics2d.setFont(font);
        graphics2d.setColor(new Color(71, 71, 71));

        FontDesignMetrics metrics = FontDesignMetrics.getMetrics(font);
        graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        String zhWrap = FontUtil.makeZhLineFeed(graphics2dPoster.getZh(), metrics, graphics2dPoster.getSuitableWidth());
        String[] zhWraps = zhWrap.split("\n");
        for (int i = 0; i < zhWraps.length; i++) {
            graphics2dPoster.addCurrentY(metrics.getHeight());
            graphics2d.drawString(zhWraps[i], MARGIN, graphics2dPoster.getCurrentY());
        }

        return graphics2dPoster;
    }

    public static Graphics2DPoster drawImage(BufferedImage bgImage, BufferedImage picImage) throws IOException {
        // 封面图的起始坐标
        int pic_x = MARGIN, pic_y = MARGIN;
        // 封面图的宽度
        int pic_width = bgImage.getWidth() - MARGIN * 2;
        // 封面图的高度
        int pic_height = picImage.getHeight() * pic_width / picImage.getWidth();

        // Graphics2D 类扩展 Graphics 类，以提供对几何形状、坐标转换、颜色管理和文本布局更为复杂的控制。
        Graphics2D graphics2d = bgImage.createGraphics();
        Graphics2DPoster graphics2dPoster = new Graphics2DPoster(graphics2d);
        // 海报可容纳的宽度
        graphics2dPoster.setSuitableWidth(pic_width);
        graphics2dPoster.setPicImage(picImage);
        graphics2dPoster.setBgImage(bgImage);

        // 在背景上绘制封面图
        graphics2d.drawImage(picImage, pic_x, pic_y, pic_width, pic_height, null);

        // 记录此时的 y 坐标
        graphics2dPoster.setCurrentY(pic_y + pic_height);

        return graphics2dPoster;
    }

    /**
     * 创建海报
     *
     * @param picURL 图片URL
     * @param zh 中文内容
     * @param en 英文内容
     * @param bgImagePath 背景图片路径（可选，为null时使用默认背景）
     * @param qrcodeImagePath 二维码图片路径（可选，为null时使用默认二维码）
     * @param outputDir 输出目录（可选，为null时使用系统临时目录）
     * @return 生成的海报文件路径
     */
    public static String create(String picURL, String zh, String en,
                                String bgImagePath, String qrcodeImagePath,
                                Path outputDir) {
        try {
            // 设置输出目录
            if (outputDir == null) {
                outputDir = FileUtil.DEFAULT_DIRECTORY;
            }

            // 确保输出目录存在
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            // 背景图片
            BufferedImage bgImage;
            if (StringUtils.isNotEmpty(bgImagePath)) {
                File customBgFile = new File(bgImagePath);
                if (customBgFile.exists()) {
                    bgImage = ImageIO.read(customBgFile);
                } else {
                    throw new IOException("背景图片不存在: " + bgImagePath);
                }
            } else {
                // 使用默认背景
                File bgFile = FileUtil.read("bg_", ".jpg", "default_bgimg.jpg");
                bgFile.deleteOnExit();
                bgImage = ImageIO.read(bgFile);
            }

            // 下载并绘制封面图
            File picFile = CapturePic.capture(picURL);
            picFile.deleteOnExit();
            BufferedImage picImage = ImageIO.read(picFile);
            Graphics2DPoster graphics2dPoster = drawImage(bgImage, picImage);

            // 绘制中文内容
            graphics2dPoster.setZh(zh);
            drawZhString(graphics2dPoster);

            // 绘制英文内容（如果有）
            if (StringUtils.isNotEmpty(en)) {
                graphics2dPoster.setEn(en);
                drawEnString(graphics2dPoster);
            }

            // 二维码
            BufferedImage qrcodeImage;
            if (StringUtils.isNotEmpty(qrcodeImagePath)) {
                File customQrcodeFile = new File(qrcodeImagePath);
                if (customQrcodeFile.exists()) {
                    qrcodeImage = ImageIO.read(customQrcodeFile);
                } else {
                    throw new IOException("二维码图片不存在: " + qrcodeImagePath);
                }
            } else {
                // 使用默认二维码
                File qrcodeFile = FileUtil.read("qrcode_", ".jpg", "default_qrcodeimg.jpg");
                qrcodeFile.deleteOnExit();
                qrcodeImage = ImageIO.read(qrcodeFile);
            }

            graphics2dPoster.setQrcodeImage(qrcodeImage);
            drawQrcode(graphics2dPoster);

            // 释放图形上下文，以及它正在使用的任何系统资源。
            graphics2dPoster.getGraphics2d().dispose();

            // 创建输出文件
            File posterFile = Files.createTempFile(outputDir, "poster_", ".jpg").toFile();
            ImageIO.write(graphics2dPoster.getBgImage(), "jpg", posterFile);

            return posterFile.getAbsolutePath();
        } catch (Exception e) {
            logger.error("海报生成失败: " + e.getMessage(), e);
            return "海报生成失败: " + e.getMessage();
        }
    }

    /**
     * 使用默认参数创建海报
     */
    public static String create(String picURL, String zh, String en) {
        return create(picURL, zh, en, null, null, null);
    }

    /**
     * 使用本地图片创建海报
     *
     * @param picPath 本地图片路径
     * @param zh 中文内容
     * @param en 英文内容
     * @param outputDir 输出目录
     * @return 生成的海报文件路径
     */
    public static String createFromLocalImage(String picPath, String zh, String en, Path outputDir) {
        try {
            if (StringUtils.isEmpty(picPath)) {
                throw new IllegalArgumentException("图片路径不能为空");
            }

            File picFile = new File(picPath);
            if (!picFile.exists()) {
                throw new IOException("图片不存在: " + picPath);
            }

            // 设置输出目录
            if (outputDir == null) {
                outputDir = FileUtil.DEFAULT_DIRECTORY;
            }

            // 确保输出目录存在
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            // 背景
            File bgFile = FileUtil.read("bg_", ".jpg", "default_bgimg.jpg");
            bgFile.deleteOnExit();
            BufferedImage bgImage = ImageIO.read(bgFile);

            // 绘制封面图
            BufferedImage picImage = ImageIO.read(picFile);
            Graphics2DPoster graphics2dPoster = drawImage(bgImage, picImage);

            // 绘制中文内容
            graphics2dPoster.setZh(zh);
            drawZhString(graphics2dPoster);

            // 绘制英文内容（如果有）
            if (StringUtils.isNotEmpty(en)) {
                graphics2dPoster.setEn(en);
                drawEnString(graphics2dPoster);
            }

            // 二维码
            File qrcodeFile = FileUtil.read("qrcode_", ".jpg", "default_qrcodeimg.jpg");
            qrcodeFile.deleteOnExit();
            BufferedImage qrcodeImage = ImageIO.read(qrcodeFile);
            graphics2dPoster.setQrcodeImage(qrcodeImage);
            drawQrcode(graphics2dPoster);

            // 释放图形上下文
            graphics2dPoster.getGraphics2d().dispose();

            // 创建输出文件
            File posterFile = Files.createTempFile(outputDir, "poster_", ".jpg").toFile();
            ImageIO.write(graphics2dPoster.getBgImage(), "jpg", posterFile);

            return posterFile.getAbsolutePath();
        } catch (Exception e) {
            logger.error("海报生成失败: " + e.getMessage(), e);
            return "海报生成失败: " + e.getMessage();
        }
    }
}
