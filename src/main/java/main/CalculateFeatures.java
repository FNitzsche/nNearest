package main;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CalculateFeatures {

    Image img;
    public CalculateFeatures(Image img){
        this.img = img;
    }

    public float[][][] calculateFeatures(Stage stage){
        Mat src = imageToMat(img);

        ArrayList<Mat> mats = new ArrayList<>();
        mats.add(src);
        mats.addAll(Arrays.asList(getObjects(src)));
        mats.addAll(Arrays.asList(smallFeatures(src)));

        ArrayList<Image> imgs = new ArrayList<>();
        for (Mat mat: mats){
            imgs.add(mat2Image(mat));
        }

        if (stage != null) {
            stage.setScene(new Scene(new HBox(new ImageView(mat2Image(mats.get(6))))));
            stage.show();
        }

        float[][][] ret = new float[(int)img.getWidth()][(int)img.getHeight()][mats.size()*3];

        for (int i = 0; i < img.getWidth(); i++){
            for (int j = 0; j < img.getHeight(); j++){
                ret[i][j] = new float[]{
                        (float) imgs.get(0).getPixelReader().getColor(i, j).getRed(), (float) imgs.get(0).getPixelReader().getColor(i, j).getGreen(),(float) imgs.get(0).getPixelReader().getColor(i, j).getBlue(),
                        (float) 0,(float) 0, (float) imgs.get(1).getPixelReader().getColor(i, j).getRed(),
                        (float) imgs.get(2).getPixelReader().getColor(i, j).getRed(), (float) imgs.get(2).getPixelReader().getColor(i, j).getGreen(),(float) imgs.get(2).getPixelReader().getColor(i, j).getBlue(),
                        (float) imgs.get(3).getPixelReader().getColor(i, j).getRed(), (float) imgs.get(3).getPixelReader().getColor(i, j).getGreen(),(float) imgs.get(3).getPixelReader().getColor(i, j).getBlue(),
                        (float) imgs.get(4).getPixelReader().getColor(i, j).getRed(), (float) imgs.get(4).getPixelReader().getColor(i, j).getGreen(),(float) imgs.get(4).getPixelReader().getColor(i, j).getBlue(),
                        (float) imgs.get(5).getPixelReader().getColor(i, j).getRed(), (float) imgs.get(5).getPixelReader().getColor(i, j).getGreen(),(float) imgs.get(5).getPixelReader().getColor(i, j).getBlue(),
                        (float) imgs.get(6).getPixelReader().getColor(i, j).getRed(), (float) imgs.get(6).getPixelReader().getColor(i, j).getGreen(),(float) imgs.get(6).getPixelReader().getColor(i, j).getBlue(),
                };
            }
        }


        return ret;
    }

    public Mat[] smallFeatures(Mat src){
        Mat medianSmall = new Mat();
        Imgproc.medianBlur(src, medianSmall, 3);
        Mat medianMedium = new Mat();
        Imgproc.medianBlur(src, medianMedium, 5);
        Mat medianBig = new Mat();
        Imgproc.medianBlur(src, medianBig, 9);
        Mat hsv = new Mat();
        Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);
        return new Mat[]{medianSmall, medianMedium, medianBig, hsv};
    }




    // from https://docs.opencv.org/master/d2/dbd/tutorial_distance_transform.html

    public Mat[] getObjects(Mat srcOriginal){
        // Change the background from white to black, since that will help later to
        // extract
        // better results during the use of Distance Transform
        Mat src = srcOriginal.clone();
        byte[] srcData = new byte[(int) (src.total() * src.channels())];
        src.get(0, 0, srcData);
        for (int i = 0; i < src.rows(); i++) {
            for (int j = 0; j < src.cols(); j++) {
                if (srcData[(i * src.cols() + j) * 3] == (byte) 255 && srcData[(i * src.cols() + j) * 3 + 1] == (byte) 255
                        && srcData[(i * src.cols() + j) * 3 + 2] == (byte) 255) {
                    srcData[(i * src.cols() + j) * 3] = 0;
                    srcData[(i * src.cols() + j) * 3 + 1] = 0;
                    srcData[(i * src.cols() + j) * 3 + 2] = 0;
                }
            }
        }
        src.put(0, 0, srcData);
        // Show output image
        HighGui.imshow("Black Background Image", src);
        // Create a kernel that we will use to sharpen our image
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        // an approximation of second derivative, a quite strong kernel
        float[] kernelData = new float[(int) (kernel.total() * kernel.channels())];
        kernelData[0] = 1; kernelData[1] = 1; kernelData[2] = 1;
        kernelData[3] = 1; kernelData[4] = -8; kernelData[5] = 1;
        kernelData[6] = 1; kernelData[7] = 1; kernelData[8] = 1;
        kernel.put(0, 0, kernelData);
        // do the laplacian filtering as it is
        // well, we need to convert everything in something more deeper then CV_8U
        // because the kernel has some negative values,
        // and we can expect in general to have a Laplacian image with negative values
        // BUT a 8bits unsigned int (the one we are working with) can contain values
        // from 0 to 255
        // so the possible negative number will be truncated
        Mat imgLaplacian = new Mat();
        Imgproc.filter2D(src, imgLaplacian, CvType.CV_32F, kernel);
        Mat sharp = new Mat();
        src.convertTo(sharp, CvType.CV_32F);
        Mat imgResult = new Mat();
        Core.subtract(sharp, imgLaplacian, imgResult);
        // convert back to 8bits gray scale
        Mat tmpResult = new Mat(imgResult.size(), CvType.CV_8UC3);
        System.out.println(tmpResult.type());
        Imgproc.cvtColor(imgResult,tmpResult,Imgproc.COLOR_BGRA2BGR);
        tmpResult.convertTo(tmpResult, CvType.CV_8UC3);
        imgResult.convertTo(imgResult, CvType.CV_8UC3);
        System.out.println(tmpResult.type());
        System.out.println(CvType.CV_8UC3);
        imgLaplacian.convertTo(imgLaplacian, CvType.CV_8UC3);
        // imshow( "Laplace Filtered Image", imgLaplacian );
        //HighGui.imshow("New Sharped Image", imgResult);
        // Create binary image from source image
        Mat bw = new Mat();
        Imgproc.cvtColor(imgResult, bw, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(bw, bw, 40, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        //HighGui.imshow("Binary Image", bw);
        // Perform the distance transform algorithm
        Mat dist = new Mat();
        Imgproc.distanceTransform(bw, dist, Imgproc.DIST_L2, 3);
        // Normalize the distance image for range = {0.0, 1.0}
        // so we can visualize and threshold it
        Core.normalize(dist, dist, 0.0, 1.0, Core.NORM_MINMAX);
        Mat distDisplayScaled = new Mat();
        Core.multiply(dist, new Scalar(255), distDisplayScaled);
        Mat distDisplay = new Mat();
        distDisplayScaled.convertTo(distDisplay, CvType.CV_8U);
        //HighGui.imshow("Distance Transform Image", distDisplay);
        // Threshold to obtain the peaks
        // This will be the markers for the foreground objects
        Imgproc.threshold(dist, dist, 0.4, 1.0, Imgproc.THRESH_BINARY);
        // Dilate a bit the dist image
        Mat kernel1 = Mat.ones(3, 3, CvType.CV_8U);
        Imgproc.dilate(dist, dist, kernel1);
        Mat distDisplay2 = new Mat();
        dist.convertTo(distDisplay2, CvType.CV_8U);
        Core.multiply(distDisplay2, new Scalar(255), distDisplay2);
        HighGui.imshow("Peaks", distDisplay2);
        // Create the CV_8U version of the distance image
        // It is needed for findContours()
        Mat dist_8u = new Mat();
        dist.convertTo(dist_8u, CvType.CV_8U);
        // Find total markers
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dist_8u, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        // Create the marker image for the watershed algorithm
        Mat markers = Mat.zeros(dist.size(), CvType.CV_32S);
        // Draw the foreground markers
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(markers, contours, i, new Scalar(i + 1), -1);
        }
        // Draw the background marker
        Mat markersScaled = new Mat();
        markers.convertTo(markersScaled, CvType.CV_32F);
        Core.normalize(markersScaled, markersScaled, 0.0, 255.0, Core.NORM_MINMAX);
        Imgproc.circle(markersScaled, new Point(5, 5), 3, new Scalar(255, 255, 255), -1);
        Mat markersDisplay = new Mat();
        markersScaled.convertTo(markersDisplay, CvType.CV_8U);
        HighGui.imshow("Markers", markersDisplay);
        Imgproc.circle(markers, new Point(5, 5), 3, new Scalar(255, 255, 255), -1);
        // Perform the watershed algorithm

        Imgproc.watershed(tmpResult, markers);
        Mat mark = Mat.zeros(markers.size(), CvType.CV_8U);
        markers.convertTo(mark, CvType.CV_8UC1);
        Core.bitwise_not(mark, mark);
        // imshow("Markers_v2", mark); // uncomment this if you want to see how the mark
        // image looks like at that point
        // Generate random colors
        Random rng = new Random(12345);
        List<Scalar> colors = new ArrayList<>(contours.size());
        for (int i = 0; i < contours.size(); i++) {
            int b = rng.nextInt(256);
            int g = rng.nextInt(256);
            int r = rng.nextInt(256);
            colors.add(new Scalar(b, g, r));
        }
        // Create the result image
        Mat dst = Mat.zeros(markers.size(), CvType.CV_8UC3);
        byte[] dstData = new byte[(int) (dst.total() * dst.channels())];
        dst.get(0, 0, dstData);
        // Fill labeled objects with random colors
        int[] markersData = new int[(int) (markers.total() * markers.channels())];
        markers.get(0, 0, markersData);
        for (int i = 0; i < markers.rows(); i++) {
            for (int j = 0; j < markers.cols(); j++) {
                int index = markersData[i * markers.cols() + j];
                if (index > 0 && index <= contours.size()) {
                    dstData[(i * dst.cols() + j) * 3 + 0] = (byte) colors.get(index - 1).val[0];
                    dstData[(i * dst.cols() + j) * 3 + 1] = (byte) colors.get(index - 1).val[1];
                    dstData[(i * dst.cols() + j) * 3 + 2] = (byte) colors.get(index - 1).val[2];
                } else {
                    dstData[(i * dst.cols() + j) * 3 + 0] = 0;
                    dstData[(i * dst.cols() + j) * 3 + 1] = 0;
                    dstData[(i * dst.cols() + j) * 3 + 2] = 0;
                }
            }
        }
        dst.put(0, 0, dstData);
        return new Mat[] {distDisplay, dst};
    }






    public Mat imageToMat(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        byte[] buffer = new byte[width * height * 4];

        PixelReader reader = image.getPixelReader();
        WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraInstance();
        reader.getPixels(0, 0, width, height, format, buffer, 0, width * 4);

        Mat mat = new Mat(height, width, CvType.CV_8UC4);
        mat.put(0, 0, buffer);
        return mat;
    }

    public Image mat2Image(Mat src)
    {
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", src, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}
