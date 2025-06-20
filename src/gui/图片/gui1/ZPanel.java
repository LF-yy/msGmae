//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.gui1;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ZPanel extends JPanel {
    private static final long serialVersionUID = 6702278957072713279L;
    private Icon wallpaper;

    public ZPanel() {
    }

    protected void paintComponent(Graphics g) {
        if (null != this.wallpaper) {
            this.processBackground(g);
        }

    }

    public void setBackground(Icon wallpaper) {
        this.wallpaper = wallpaper;
        this.repaint();
    }

    private void processBackground(Graphics g) {
        ImageIcon icon = (ImageIcon)this.wallpaper;
        Image image = icon.getImage();
        int cw = this.getWidth();
        int ch = this.getHeight();
        int iw = image.getWidth(this);
        int ih = image.getHeight(this);
        int x = 0;
        int y = 0;

        while(y <= ch) {
            g.drawImage(image, x, y, this);
            x += iw;
            if (x >= cw) {
                x = 0;
                y += ih;
            }
        }

    }
}
