package info.talsemgeest.wpsorter;

import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import javax.swing.JFrame;

/**
 *
 * @author Tommy
 */
public class ImageSorter extends JFrame {

    // this line is needed to avoid serialization warnings  
    private static final long serialVersionUID = 1L;

    Image screenImage; // downloaded image  
    int w, h; // Display height and width 
    File[] images;
    File sourceFolder;
    int currentImage = -1;
    ArrayList<File[]> movedFiles;

//    public static void main(String[] args) throws Exception {
//        if (args.length < 1) // by default program will load AnyExample logo 
//        {
//            System.out.println("Please enter the directory of images as the first argument");
//        } else {
//            new ImageSorter(args[0]); // or first command-line argument 
//        }
//    }
// Class constructor  
    public ImageSorter(String source) {

        movedFiles = new ArrayList<>();
        sourceFolder = new File(source);
        populateImageList();
        if (images == null || images.length < 1) {
            System.out.println("No images found!");
            System.exit(1);
        }

        // Exiting program on window close 
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == 1) {
                    moveCurrentImage(new File(sourceFolder.getAbsolutePath() + "/keep"));
                } else if (e.getButton() == 2) {
                    moveCurrentImage(new File(sourceFolder.getAbsolutePath() + "/maybe"));
                } else if (e.getButton() == 3) {
                    moveCurrentImage(new File(sourceFolder.getAbsolutePath() + "/-deleted-/"));
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        }
        );

        addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getExtendedKeyCode() == e.VK_ENTER) {
                    moveCurrentImage(new File(sourceFolder.getAbsolutePath() + "/keep"));
                } else if (e.getExtendedKeyCode() == e.VK_SPACE) {
                    moveCurrentImage(new File(sourceFolder.getAbsolutePath() + "/-deleted-/"));
                } else if (e.getExtendedKeyCode() == e.VK_M) {
                    moveCurrentImage(new File(sourceFolder.getAbsolutePath() + "/maybe"));
                } else if (e.getExtendedKeyCode() == e.VK_DELETE) {
                    moveCurrentImage(new File(sourceFolder.getAbsolutePath() + "/-deleted-/"));
                } else if (e.getExtendedKeyCode() == e.VK_Z) {
                    if (e.isControlDown()) {
                        undo();
                    }
                } else if (e.getExtendedKeyCode() == e.VK_ESCAPE) {
                    exit();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        // remove window frame  
        this.setUndecorated(true);

        // window should be visible 
        this.setVisible(true);

        // switching to fullscreen mode 
        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);

        // getting display resolution: width and height 
        w = this.getWidth();
        h = this.getHeight();
        System.out.println("Display resolution: " + String.valueOf(w) + "x" + String.valueOf(h));

        showNextImage();

    }

    public void showNextImage() {
        currentImage += 1;
        if (currentImage != images.length) {
            screenImage = Toolkit.getDefaultToolkit().getImage(images[currentImage].getAbsolutePath());
            this.repaint();
        } else {
            try {
                //Reached the end of images
                screenImage = ImageIO.read(ClassLoader.getSystemResource("info/talsemgeest/wpsorter/assets/end.png").openStream());
            } catch (IOException ex) {
                System.out.println("Could not read end image");
            }
            this.repaint();
        }

    }

    public void moveCurrentImage(File moveFolder) {
        if (currentImage != images.length) {
            moveFolder.mkdir();
            String fName = images[currentImage].getName();
            File newFileName = new File(moveFolder.getAbsolutePath() + "/" + fName);
            File[] undoMove = {images[currentImage], newFileName};
            images[currentImage].renameTo(newFileName);
            movedFiles.add(undoMove);
            showNextImage();
        }
    }

    public void undo() {
        if (currentImage > 0) {
            System.out.println("Undoing...");
            File[] undo = movedFiles.get(movedFiles.size() - 1);
            undo[1].renameTo(undo[0]);
            currentImage -= 2;
            showNextImage();
        }
    }

    public void exit() {
        File deleteFolder = new File(sourceFolder.getAbsolutePath() + "/-deleted-/");
        if (deleteFolder.exists()) {
            File[] files = deleteFolder.listFiles();
            for (File f : files) {
                f.delete();
            }

            deleteFolder.delete();
        }

        setVisible(false);
        dispose();
    }

    public void populateImageList() {
        //Create filter for 
        FilenameFilter fnf = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name.lastIndexOf('.') > 0) {
                    // get last index for '.' char
                    int lastIndex = name.lastIndexOf('.');

                    // get extension
                    String str = name.substring(lastIndex);

                    // match path name extension
                    if (str.equals(".jpg") || str.equals(".png") || str.equals(".jpeg")) {
                        return true;
                    }
                }
                return false;
            }
        };
        images = sourceFolder.listFiles(fnf);
    }

    public void paint(Graphics g) {
        if (screenImage != null) // if screenImage is not null (image loaded and ready) 
        {
            g.drawImage(screenImage, // draw it  
                    w / 2 - screenImage.getWidth(this) / 2, // at the center  
                    h / 2 - screenImage.getHeight(this) / 2, // of screen 
                    this);
        }
        // to draw image at the center of screen 
        // we calculate X position as a half of screen width minus half of image width 
        // Y position as a half of screen height minus half of image height 
    }

}
