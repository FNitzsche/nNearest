package main;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ExportSVG {

    public static void exportSVG(ArrayList<ArrayList<double[]>> allPaths, String path, String prefix, int r, int resX, int resY, float[][] centers, boolean simplified, ArrayList<Image> images, Image[] imgs){
        String p = path + "\\" + prefix.substring(0, prefix.lastIndexOf(".")) + ".svg";
        try (PrintWriter out = new PrintWriter(p)) {
            out.println("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"" + resX + "\" height=\"" + resY + "\">");

            for (int i = 0; i < allPaths.size(); i++){
                writeCluster(out, allPaths, r, centers, simplified, i, images, imgs);
            }

            out.println("</svg>");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeCluster(PrintWriter out, ArrayList<ArrayList<double[]>> allPaths, int r, float[][] centers, boolean simplified, int i, ArrayList<Image> images, Image[] imgs){
        ArrayList<double[]> activePath = allPaths.get(i);
        float[] activeCluster = centers[images.indexOf(imgs[i])];

        int pos = 0;
        ArrayList<double[]> coPath = new ArrayList<>();
        for (double[] point: activePath){
            //System.out.println(point[2]);
            if (coPath.size() == 0){
                coPath.add(point);
            } else if (point[2] == coPath.get(coPath.size()-1)[2]){
                //System.out.println("added");
                coPath.add(point);
            } else {
                if (coPath.get(0)[2] == 1){
                    if (!simplified){

                    }
                } else if (coPath.get(0)[2] == 2){
                    drawContour(out, coPath, activeCluster, simplified, r);
                }
                coPath.clear();
                coPath.add(point);
            }
        }

    }

    public static void drawContour(PrintWriter out, ArrayList<double[]> contour, float[] activeCluster, boolean simplified, int r){
        out.println("<path d=\" M " + contour.get(0)[0] + "," + contour.get(0)[1] + " L ");

        StringBuilder line = new StringBuilder();

        for (int i = 1; i < contour.size(); i++){
            line.append(" ").append(contour.get(i)[0]).append(",").append(contour.get(i)[1]);
        }
        out.println(line);
        out.println("\"");
        out.println("stroke = \"black\" stroke-width=\"" + ((r/4)+1) + "\"");
        if (simplified){
            out.println("fill = \"" + toRGBCode(Color.color(activeCluster[0], activeCluster[1], activeCluster[2])) + "\"");
        }
        out.println("/>");
    }

    public static String toRGBCode( Color color )
    {
        return String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );
    }

}
