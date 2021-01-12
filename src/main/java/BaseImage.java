import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.net.URL;
import java.util.ArrayList;

public class BaseImage{

    Image original;
    Image preview;
    String path;

    float[][][] fullSize;
    float[][][] preSize;

    public boolean loaded = false;

    public BaseImage(String path, int preResX, int preResY){
        this.path = path;
        loaded = loadImage(preResX, preResY);
    }


    public boolean loadImage(int preResX, int preResY){
        Image tmp = null;
        Image tmpP = null;
        try {
            URL url = new URL("file:" + path);
            tmp = new Image(url.toString());
            tmpP = new Image(url.toString(), preResX, preResY, true, false);
        } catch (Exception e){
            System.out.println(e);
        }
        if(null == tmpP || null == tmp){
            System.out.println("no Preview");
            return false;
        } else {
            original = tmp;
            preview = tmpP;
        }
        preSize = imgToArray(preview);
        fullSize = imgToArray(original);
        return true;
    }

    public float[][][] imgToArray(Image img){
        float[][][] ret = new float[(int)img.getWidth()][(int)img.getHeight()][3];
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                ret[i][j][0] = (float) img.getPixelReader().getColor(i, j).getRed();
                ret[i][j][1] = (float) img.getPixelReader().getColor(i, j).getGreen();
                ret[i][j][2] = (float) img.getPixelReader().getColor(i, j).getBlue();
            }
        }
        return ret;
    }

    public float[][][] getPreSize(){
        return imgToArray(preview);
    }

    public float[][][] getFullSize(){
        return imgToArray(original);
    }

    @Override
    public String toString(){
        return path.substring(path.lastIndexOf('\\'), path.length());
    }

    public Image showImage(boolean fullRun) {
        if (!preview.isError() && !original.isError()) {
            if (!fullRun) {
                return preview;
            } else {
                return original;
            }
        }
        return null;
    }
}
