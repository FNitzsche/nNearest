import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class PathGen {

    public PathGen(){
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
    }

    public ArrayList<ArrayList<double[]>> pathGen(Image[] imgs, int r){
        ArrayList<ArrayList<double[]>> ret = new ArrayList<>();
        HashMap<Integer, ArrayList<double[]>> tmpRet = new HashMap<>();
        HashMap<Integer, Image> tmpImg = new HashMap<>();
        ArrayList<int[]> areaKey = new ArrayList<>();
        for (Image img: imgs) {
            float area = 0;
            Mat imageMatrix = imageToMat(img);
            Mat gray = new Mat(imageMatrix.rows(), imageMatrix.cols(), imageMatrix.type());
            Imgproc.cvtColor(imageMatrix, gray, Imgproc.COLOR_BGR2GRAY);
            Mat binary = new Mat(imageMatrix.rows(), imageMatrix.cols(), imageMatrix.type(), new Scalar(0));
            Imgproc.threshold(gray, binary, 100, 255, Imgproc.THRESH_BINARY);
            //Finding Contours
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hirarchy = new Mat();
            Imgproc.findContours(binary, contours, hirarchy, Imgproc.RETR_TREE,
                    Imgproc.CHAIN_APPROX_NONE);
            for (MatOfPoint con: contours){
                area += Imgproc.contourArea(con, true);
            }
            ArrayList<double[]> list = drawPath(contours, r, hirarchy);
            areaKey.add(new int[]{(int)area, list.hashCode()});
            tmpRet.put(list.hashCode(), list);
            tmpImg.put(list.hashCode(), img);
        }
        Collections.sort(areaKey, new Comparator<int[]>() {
            @Override
            public int compare(int[] ints, int[] t1) {
                return Integer.compare(ints[0], t1[0]);
            }
        });
        for (int i = 0; i < areaKey.size(); i++){
            ret.add(tmpRet.get(areaKey.get(i)[1]));
            imgs[i] = tmpImg.get(areaKey.get(i)[1]);
        }
        return ret;
    }

    public ArrayList<double[]> drawPath(List<MatOfPoint> contours, int r, Mat hirarchy){
        ArrayList<double[]> path = new ArrayList<>();
        path.add(new double[]{0, 0, 0});
        for (MatOfPoint contour: contours){
            path.addAll(contour.toList().stream().map(p -> new double[] {p.x, p.y, 1}).collect(Collectors.toList()));
            path.add(new double[]{0, 0, 0});
            ArrayList<Point> first = new ArrayList<>(contour.toList().subList(0,contour.toList().size()/2 ));
            ArrayList<Point> second = new ArrayList<>(contour.toList());
            second.removeAll(first);
            if (first.size()-second.size() < 0){
                ArrayList<Point> tmp = second;
                second = first;
                first = tmp;
            }
            Collections.reverse(second);
            if (first.size() > 0 && second.size() > 0){
                //System.out.println(first.size()-second.size());
                boolean running = true;
                int i = 1;
                while (running){
                    running = false;
                    for (int j = 0; j < first.size(); j++){
                        Point p = first.get(j);
                        Point q;
                        if (j < second.size()){
                            q = second.get(j);
                        } else {
                            q = second.get(second.size()-1);
                        }
                        float dist = (float) Math.sqrt(Math.pow(p.x-q.x, 2) + Math.pow(p.y-q.y, 2));
                        float t = (i*r)/dist;
                        if (t >= 1){
                            path.add(new double[]{0, 0, 0});
                        } else {
                            path.add(new double[]{q.x+(p.x-q.x)*t, q.y+(p.y-q.y)*t, 1});
                            running = true;
                        }
                    }
                    path.add(new double[]{0, 0, 0});
                    i++;
                }
            }
        }
        return path;
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
