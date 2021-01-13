import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoWriter;

import javax.imageio.ImageIO;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class CreateDrawAnimation {

    PathGen pg = new PathGen();

    public void saveAnimation(String savePath, String prefix, float[][][] imgArray, int n, int frameCount, int r, int resX, int resY, float[][] centers, float fps){
        Image[] imgs = drawClusters(imgArray, n);
        ArrayList<ArrayList<double[]>> allPaths = pg.pathGen(imgs, r);
        cleanPaths(allPaths, r);
        ArrayList<double[]> allPoints = new ArrayList<>();
        int pointCount = 0;
        for (ArrayList<double[]> path: allPaths){
            for (double[] point: path){
                if (point[2] == 1 || point[2] == 0){
                    pointCount++;
                }
            }
            allPoints.addAll(path);
        }

        Mat img = pg.imageToMat(drawArray(imgArray));
        float pointsPerFrame = pointCount/(float)frameCount;

        int lastFrame = frameLoop(frameCount, pointsPerFrame, img, imgs, allPaths, pointCount, allPoints, savePath, prefix, r, fps);

        //saveImage(savePath, prefix, lastFrame, drawArray(imgArray));
    }

    public void cleanPaths(ArrayList<ArrayList<double[]>> allPaths, float r){
        r = Math.min(r/3, 5);
        for (int i = 0; i < allPaths.size(); i++){
            ArrayList<double[]> toKeep = new ArrayList<>();
            for (int j = 0; j < allPaths.get(i).size(); j++){
                if (j > 0){
                    double dist = r*3;
                    if (toKeep.size() > 0) {
                        dist = Math.sqrt(Math.pow(allPaths.get(i).get(j)[0] - toKeep.get(toKeep.size() - 1)[0], 2) + Math.pow(allPaths.get(i).get(j)[1] - toKeep.get(toKeep.size() - 1)[1], 2));
                    }
                    if (!(allPaths.get(i).get(j-1)[2] == 0 && allPaths.get(i).get(j)[2] == 0) && dist > r){
                        toKeep.add(allPaths.get(i).get(j));
                    }
                }
            }
            allPaths.get(i).clear();
            allPaths.get(i).addAll(toKeep);
        }
    }

    public int frameLoop(int frameCount, float pointsPerFrame, Mat img, Image[] imgs, ArrayList<ArrayList<double[]>> allPaths,
                          int pointCount, ArrayList<double[]> allPoints, String savePath, String prefix, int r, float fps){

            int active = 0;
            int past = 0;
            int f = 0;
            boolean skip = false;
            double[] lastPoint = new double[3];

        VideoWriter videoWriter;
        String p = savePath + "\\" + prefix;
        videoWriter = new VideoWriter(p, VideoWriter.fourcc('x', '2','6','4'), fps, img.size());

        Mat mask = new Mat(img.rows(), img.cols(), CvType.CV_8U, Scalar.all(0));
        Mat tmp = new Mat(img.rows(), img.cols(), CvType.CV_8U, Scalar.all(0));
        Mat baseMask = imageToMask(imgs[0]);
        Core.multiply(baseMask, mask, mask);

        for (int k = 0; k < pointCount; k++){
            if (k-past >= allPaths.get(active).size()){
                past += allPaths.get(active).size();
                active++;
                Core.multiply(baseMask, mask, mask);
                Core.add(baseMask, imageToMask(imgs[active]), baseMask);
            }

            if (k < pointCount){
                if (allPoints.get(k)[2] == 0){
                    skip = true;
                } else if (skip){
                    lastPoint = allPoints.get(k);
                    skip = false;
                } else {
                    Imgproc.line(mask, new Point(allPoints.get(k)[0], allPoints.get(k)[1]), new Point(lastPoint[0], lastPoint[1]), new Scalar(255), r);
                    lastPoint = allPoints.get(k);
                }
            }

            if (k%pointsPerFrame < 1){
                Core.multiply(baseMask, mask, tmp);
                Mat cropped = new Mat();
                img.copyTo(cropped, tmp);
                if(videoWriter.isOpened()==false){
                    videoWriter.release();
                    throw new IllegalArgumentException("Video Writer Exception: VideoWriter not opened,"
                            + "check parameters.");
                }
                //Write video
                videoWriter.write(cropped);
                f++;
            }
        }

        if(videoWriter.isOpened()==false){
            videoWriter.release();
            throw new IllegalArgumentException("Video Writer Exception: VideoWriter not opened,"
                    + "check parameters.");
        }
        //Write video
        for (int i = 0; i < fps; i++) {
            videoWriter.write(img);
        }

        videoWriter.release();
        return f;
    }


    public void saveImage(String path, String prefix, Image img){
        String p = path + "\\" + prefix;
        File file = new File(p);
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

    public Mat imageToMask(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        byte[] buffer = new byte[width * height * 4];

        PixelReader reader = image.getPixelReader();
        WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraInstance();
        reader.getPixels(0, 0, width, height, format, buffer, 0, width*4);

        Mat mat = new Mat(height, width, CvType.CV_8UC4);
        Mat gray = new Mat(height, width, CvType.CV_8U);
        mat.put(0, 0, buffer);
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        return gray;
    }

}
