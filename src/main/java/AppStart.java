import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.opencv.core.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class AppStart extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        PathGen pg = new PathGen();
        Canvas canvas = new Canvas(1280, 720);
        BaseImage base;// = new BaseImage("C:\\Users\\felix\\IdeaProjects\\nNearest\\src\\main\\resources\\DSC_3545.JPG", 1920, 1080);
        stage.setScene(new Scene(new HBox(canvas)));
        stage.show();

        //for (int i = 0; i < 30; i++) {
            base = new BaseImage("C:\\Users\\felix\\IdeaProjects\\nNearest\\src\\main\\resources\\DSC_3545.JPG", 1280, 720);
            float[][][] imgArray = base.preSize;
            imgArray = NNearestN.cluster(imgArray, 2, 5, imgArray.length, imgArray[0].length, 0, false, 0.1f, false);
            Image img = drawArray(imgArray);
            Image[] imgCluster = drawClusters(imgArray, 5);
            canvas.getGraphicsContext2D().drawImage(imgCluster[0], 0, 0);

        ArrayList<ArrayList<double[]>> allPaths = pg.pathGen(imgCluster);
        Random ran = new Random();
        for (ArrayList<double[]> path: allPaths){
            canvas.getGraphicsContext2D().setFill(Color.color(ran.nextFloat(), 0, 1));
            for (double[] point: path){
                if (point[2] == 1) {
                    canvas.getGraphicsContext2D().fillRect(point[0], point[1], 4, 4);
                }
            }
        }

            /*String p = "C:\\Users\\felix\\IdeaProjects\\nNearest\\processedImg\\" + "single 3";
            File file = new File(p + ".png");
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file);
            } catch (Exception s) {
                System.out.println(s);
            }*/
        //}

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
}
