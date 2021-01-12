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
    public static int resX = 1980;
    public static int resY = 1080;
    public static int n = 30;

    @Override
    public void start(Stage stage) throws Exception {

        Canvas canvas = new Canvas(resX, resY);
        BaseImage base;// = new BaseImage("C:\\Users\\felix\\IdeaProjects\\nNearest\\src\\main\\resources\\DSC_3545.JPG", 1920, 1080);
        stage.setScene(new Scene(new HBox(canvas)));
        stage.show();

        //for (int i = 0; i < 30; i++) {
            base = new BaseImage("C:\\Users\\felix\\IdeaProjects\\nNearest\\src\\main\\resources\\DSCPDC_0003_BURST20200917142311023_COVER.JPG", resX, resY);
            float[][][] imgArray = base.preSize;
            float[][] centers = null;
            imgArray = NNearestN.cluster(imgArray, n, 5, imgArray.length, imgArray[0].length, 0, false, 0.25f, false);
            centers = NNearestN.lastClusters;
            Image img = drawArray(imgArray);
            canvas.getGraphicsContext2D().drawImage(img, 0, 0);

            /*String p = "C:\\Users\\felix\\IdeaProjects\\nNearest\\processedImg\\" + "single 3";
            File file = new File(p + ".png");
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file);
            } catch (Exception s) {
                System.out.println(s);
            }*/
        //}
        CreateDrawAnimation cAni = new CreateDrawAnimation();
        cAni.saveAnimation("C:\\Users\\felix\\IdeaProjects\\nNearest\\processedImg\\", "drawing3_", imgArray, n, 1000, 20, resX, resY, centers, 30);

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
