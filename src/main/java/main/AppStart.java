package main;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class AppStart extends Application {
    public static int resX = 1980;
    public static int resY = 1080;
    public static int n = 30;

    FXMLLoad mainScreenFXML;

    BaseImage base = null;
    CreateDrawAnimation cAni = new CreateDrawAnimation();

    @Override
    public void start(Stage stage) throws Exception {
        mainScreenFXML = new FXMLLoad("/MainScreen.fxml", new MainScreenCon(stage, this));
        stage.setScene(mainScreenFXML.getScene());
        stage.show();

        /*base = new main.BaseImage("C:\\Users\\felix\\IdeaProjects\\nNearest\\src\\main\\resources\\DSCPDC_0003_BURST20200917142311023_COVER.JPG", resX, resY);
        float[][][] imgArray = base.preSize;
        float[][] centers = null;
        imgArray = main.NNearestN.cluster(imgArray, n, 5, imgArray.length, imgArray[0].length, 0, false, 0.25f, false);
        centers = main.NNearestN.lastClusters;
        Image img = drawArray(imgArray);
        canvas.getGraphicsContext2D().drawImage(img, 0, 0);*/


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
