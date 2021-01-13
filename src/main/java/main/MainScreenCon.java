package main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MainScreenCon {

    FileChooser fileChooser = new FileChooser();
    FileChooser saveAniChooser = new FileChooser();
    Stage stage;
    AppStart app;

    String loadPath = "";
    int resXi = 1920, resYi = 1080, nI = 5, repsI = 5, framesI = 100, seedI = 0, rI = 20;
    float spaceI = 0.3f;

    @FXML
    Button open;
    @FXML
    Button preview;
    @FXML
    Button previewAni;
    @FXML
    Button saveImg;
    @FXML
    Button saveAni;

    @FXML
    TextField n;
    @FXML
    TextField reps;
    @FXML
    TextField resX;
    @FXML
    TextField frames;
    @FXML
    TextField seed;
    @FXML
    TextField space;
    @FXML
    TextField resY;
    @FXML
    TextField r;

    @FXML
    CheckBox hue;
    @FXML
    CheckBox clusterHue;

    @FXML
    Canvas originalC;
    @FXML
    Canvas finished;


    public MainScreenCon(Stage stage, AppStart app){
        this.stage = stage;
        this.app = app;

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.JPEG"));
        saveAniChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Animation", "*.mp4"));
    }

    public void initialize(){

        n.setOnKeyTyped(t->changed = true);
        reps.setOnKeyTyped(t->changed = true);
        resX.setOnKeyTyped(t->changed = true);
        frames.setOnKeyTyped(t->changed = true);
        seed.setOnKeyTyped(t->changed = true);
        space.setOnKeyTyped(t->changed = true);
        resY.setOnKeyTyped(t->changed = true);
        r.setOnKeyTyped(t->changed = true);
        hue.setOnAction(t->changed = true);
        clusterHue.setOnAction(t->changed = true);

        open.setOnAction(t -> {
            app.base = new BaseImage(fileChooser.showOpenDialog(stage).getAbsolutePath(), resXi, resYi);
            originalC.getGraphicsContext2D().drawImage(app.base.preview, 0, 0, originalC.getWidth(), originalC.getHeight());
        });

        preview.setOnAction(t -> render(System.getProperty("user.dir"), "previewVideo_video.mp4", false));
        previewAni.setOnAction(t -> showAnimation(System.getProperty("user.dir"), "previewVideo_video.mp4"));

        saveImg.setOnAction(t -> saveImg());
        saveAni.setOnAction(t -> saveAni());

    }

    boolean changed = true;

    public void render(String path, String prefix, boolean forceRender){
        if ((changed || forceRender) && app.base.loaded) {
            changed = false;
            parseInputs();
            originalC.getGraphicsContext2D().drawImage(app.base.preview, 0, 0, originalC.getWidth(), originalC.getHeight());
            float[][][] imgArray = clusterImg();
            float[][] centers = null;
            centers = NNearestN.lastClusters;
            Image img = app.drawArray(imgArray);
            finished.getGraphicsContext2D().drawImage(img, 0, 0, originalC.getWidth(), originalC.getHeight());
            app.cAni.saveAnimation(path, prefix, imgArray, nI, framesI, rI, resXi, resYi, centers, 30);
        } else if (!app.base.loaded){
            Platform.runLater(new Runnable() {
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("No Image loaded");
                    alert.setHeaderText("Please choose a Image first.");
                    alert.showAndWait();
                }
            });
        }
    }

    public float[][][] clusterImg(){
        app.base.loadImage(resXi, resYi);
        float[][][] imgArray = app.base.preSize;
        imgArray = NNearestN.cluster(imgArray, nI, repsI, imgArray.length, imgArray[0].length,
                seedI, hue.isSelected(), spaceI, clusterHue.isSelected());
        return imgArray;
    }

    public void showAnimation(String path, String prefix){
        if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            try {
                String p = path + "\\" + prefix;
                File file = new File(p);
                desktop.open(file);
            } catch (IOException e) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Animation not Found");
                        alert.setHeaderText("Please render first.");
                        alert.showAndWait();
                    }
                });
            }
        }
    }


    public void parseInputs(){
        try {
            resXi = Integer.parseInt(resX.getText());
            resYi = Integer.parseInt(resY.getText());
        } catch (NumberFormatException e){
            resXi = 1920;
            resYi = 1080;
            Platform.runLater(new Runnable() {
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Invalid Resolution");
                    alert.setHeaderText("Enter a Resolution like: x = 1280, y = 720");
                    alert.showAndWait();
                }
            });
        };
        try {
            nI = Integer.parseInt(n.getText());
        } catch (NumberFormatException e){
            nI = 5;
            Platform.runLater(new Runnable() {
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Invalid Number of Colors");
                    alert.setHeaderText("Enter a positive Integer");
                    alert.showAndWait();
                }
            });
        };
        try {
            repsI = Integer.parseInt(reps.getText());
        } catch (NumberFormatException e){
            repsI = 5;
            Platform.runLater(new Runnable() {
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Invalid Number of Repetitions");
                    alert.setHeaderText("Enter a positive Integer");
                    alert.showAndWait();
                }
            });
        };
        try {
            framesI = Integer.parseInt(frames.getText());
        } catch (NumberFormatException e){
            repsI = 5;
            Platform.runLater(new Runnable() {
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Invalid Number of Frames");
                    alert.setHeaderText("Enter a positive Integer");
                    alert.showAndWait();
                }
            });
        };
        try {
            seedI = Integer.parseInt(seed.getText());
        } catch (NumberFormatException e){
            repsI = 5;
            Platform.runLater(new Runnable() {
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Invalid Seed");
                    alert.setHeaderText("Enter a Integer");
                    alert.showAndWait();
                }
            });
        };
        try {
            rI = Integer.parseInt(r.getText());
        } catch (NumberFormatException e){
            repsI = 5;
            Platform.runLater(new Runnable() {
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Invalid Stroke Width");
                    alert.setHeaderText("Enter a positive Integer");
                    alert.showAndWait();
                }
            });
        };
        try {
            spaceI = Float.parseFloat(space.getText());
        } catch (NumberFormatException e){
            repsI = 5;
            Platform.runLater(new Runnable() {
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Invalid Voronoi-Factor");
                    alert.setHeaderText("Enter a positive Number between 0 and 2");
                    alert.showAndWait();
                }
            });
        };
    }

    public void saveImg(){
        String fullPath = fileChooser.showSaveDialog(stage).getAbsolutePath();
        String path = fullPath.substring(0, fullPath.lastIndexOf("\\"));
        String prefix = fullPath.substring(fullPath.lastIndexOf("\\")+1);
        parseInputs();
        app.cAni.saveImage(path, prefix, app.drawArray(clusterImg()));

    }

    public void saveAni(){
        String fullPath = saveAniChooser.showSaveDialog(stage).getAbsolutePath();
        String path = fullPath.substring(0, fullPath.lastIndexOf("\\"));
        String prefix = fullPath.substring(fullPath.lastIndexOf("\\")+1);
        render(path, prefix, true);

    }

}