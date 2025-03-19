package com.lhk.poster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.awt.image.BufferedImage;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Graphics2DPoster {
    private Graphics2D graphics2d;
    private int suitableWidth;
    private int currentY;
    private String zh;
    private String en;
    private BufferedImage bgImage;
    private BufferedImage qrcodeImage;
    private BufferedImage picImage;

    public Graphics2DPoster(Graphics2D graphics2d) {
        super();
        this.graphics2d = graphics2d;
    }

    public void addCurrentY(int y) {
        setCurrentY(getCurrentY() + y);
    }
}
