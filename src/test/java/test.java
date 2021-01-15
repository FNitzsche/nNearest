import javafx.application.Application;
import javafx.stage.Stage;
import main.BaseImage;
import main.CalculateFeatures;
import org.junit.Test;
import org.opencv.core.Core;

public class test extends Application {

    public void test(Stage stage){
        BaseImage base = new BaseImage("C:\\Users\\felix\\IdeaProjects\\nNearest\\src\\test\\resources\\DSC_3611.JPG", 1920, 1080);
        CalculateFeatures cF = new CalculateFeatures(base.showImage(false));
        System.out.println("blubb1");
        cF.calculateFeatures(stage);
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        test(stage);
    }
}
