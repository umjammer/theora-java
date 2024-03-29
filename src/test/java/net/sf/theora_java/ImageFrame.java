package net.sf.theora_java;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


/**
 * Adapted from http://www.oreilly.com/catalog/learnjava/chapter/ch14.html
 *
 * @author Ken Larson
 */
public class ImageFrame extends JFrame {

    public static void main(String[] args) {
        String filename = "/Users/ken/Documents/workspace/vx.gui.standalone/sampledata/Stefan_Raab.jpg";
        if (args.length > 0)
            filename = args[0];

        ImageFrame imageFrame = new ImageFrame("ImageFrame");

        imageFrame.setSize(300, 300);
        imageFrame.setLocation(200, 200);
        imageFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        Image image = Toolkit.getDefaultToolkit().getImage(filename);
        imageFrame.setImage(image);

        imageFrame.setVisible(true);
        imageFrame.pack();
    }

    public void setImage(Image image) {
        ic.setImage(image);
    }

    private ImageComponent ic;

    public ImageFrame(String title) {
        super(title);

        ic = new ImageComponent();
        getContentPane().add(ic);

    }

    public void setImageSize(int h, int w) {
        ic.setImageSize(new Dimension(h, w));
    }

    public class ImageComponent extends JComponent {

        private Image image;

        private Dimension size;

        public void setImage(Image image) {
            SwingUtilities.invokeLater(new ImageRunnable(image));

        }

        public void setImageSize(Dimension newSize) {
            if (!newSize.equals(size)) {    //System.out.println("New size " + newSize + " from " + size);
                size = newSize;
                setSize(size);
                ImageFrame.this.pack();
            }
        }

        private class ImageRunnable implements Runnable {

            private final Image newImage;

            public ImageRunnable(Image newImage) {
                super();
                this.newImage = newImage;
            }

            @Override
            public void run() {
                setImageInSwingThread(newImage);
            }

        }

        private synchronized void setImageInSwingThread(Image image) {
            this.image = image;
            Dimension newSize = new Dimension(image.getWidth(null), image.getHeight(null));
            setImageSize(newSize);
            repaint();
        }

        public ImageComponent() {
            size = new Dimension(0, 0);
            setSize(size);
        }

        @Override
        public synchronized void paint(Graphics g) {
            if (image != null)
                g.drawImage(image, 0, 0, this);
        }

        @Override
        public synchronized Dimension getPreferredSize() {
            return size;
        }
    }
}
