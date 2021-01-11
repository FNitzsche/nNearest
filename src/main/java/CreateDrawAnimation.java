import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;

public class CreateDrawAnimation {

    PathGen pg = new PathGen();

    public void saveAnimation(String savePath, String prefix, float[][][] imgArray, int n, int frameCount, int r, int resX, int resY){
        Image[] imgs = drawClusters(imgArray, n);
        ArrayList<ArrayList<double[]>> allPaths = pg.pathGen(imgs);
        ArrayList<double[]> allPoints = new ArrayList<>();
        int pointCount = 0;
        for (ArrayList<double[]> path: allPaths){
            for (double[] point: path){
                if (point[2] == 1){
                    pointCount++;
                }
            }
            allPoints.addAll(path);
        }

        Mat img = pg.imageToMat(drawArray(imgArray));
        float pointsPerFrame = pointCount/(float)frameCount;
        for (int f = 0; f < frameCount; f++){
            System.out.println("Frame: " + f);
            boolean skip = false;
            double[] lastPoint = new double[3];
            Mat mask = new Mat(img.rows(), img.cols(), CvType.CV_8U, Scalar.all(0));
            float limit = f*pointsPerFrame;
            for (int k = 0; k < limit; k++){
                if (k < pointCount){
                    if (allPoints.get(k)[2] == 0){
                        skip = true;
                        limit++;
                        continue;
                    }
                    if (skip){
                        lastPoint = allPoints.get(k);
                        skip = false;
                        continue;
                    }
                    Imgproc.line(mask, new Point(allPoints.get(k)[0], allPoints.get(k)[1]), new Point(lastPoint[0], lastPoint[1]), new Scalar(255, 255, 255), r);
                    lastPoint = allPoints.get(k);
                }
            }
            Mat cropped = new Mat();
            img.copyTo(cropped, mask);
            saveImage(savePath, prefix, f, pg.mat2Image(cropped));
        }

    }


    public void saveImage(String path, String prefix, int f, Image img){
        String p = path + prefix + f;
        File file = new File(p + ".png");
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file);
        } catch (Exception s) {
            System.out.println(s);
        }
    }

    public Image[] drawClusters(float[][][] img, int n){
        Image[] imgs = new Image[n];
        for (int k = 0; k < n; k++) {
            WritableImage wimg = new WritableImage(img.length, img[0].length);
            for (int i = 0; i < img.length; i++) {
                for (int j = 0; j < img[0].length; j++) {
                    if (img[i][j][4] == k) {
                        wimg.getPixelWriter().setColor(i, j, Color.WHITE);
                    } else {
                        wimg.getPixelWriter().setColor(i, j, Color.BLACK);
                    }
                }
            }
            imgs[k] = wimg;
        }
        return imgs;
    }

    public Image drawArray(float[][][] img){
        WritableImage wimg = new WritableImage(img.length, img[0].length);
        for (int i = 0; i < img.length; i++){
            for (int j = 0; j < img[0].length; j++){
                wimg.getPixelWriter().setColor(i, j, Color.color(img[i][j][0], img[i][j][1], img[i][j][2]));
            }
        }
        return wimg;
    }

}
