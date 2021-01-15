package main;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.opencv.core.Core;

public class AppStart extends Application {
    public static int resX = 1980;
    public static int resY = 1080;
    public static int n = 30;

    FXMLLoad mainScreenFXML;

    BaseImage base = null;
    CreateDrawAnimation cAni = new CreateDrawAnimation();

    @Override
    public void start(Stage stage) throws Exception {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        mainScreenFXML = new FXMLLoad("/MainScreen.fxml", new MainScreenCon(stage, this));
        stage.setScene(mainScreenFXML.getScene());
        stage.show();


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
