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

            int c = 0;
            for (int i = 0; i < allPaths.size(); i++){
               c +=  writeCluster(out, allPaths, r, centers, simplified, i, images, imgs);
            }
            System.out.println("Pfade: " + c);
            out.println("</svg>");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static int writeCluster(PrintWriter out, ArrayList<ArrayList<double[]>> allPaths, int r, float[][] centers, boolean simplified, int i, ArrayList<Image> images, Image[] imgs){
        ArrayList<double[]> activePath = allPaths.get(i);
        float[] activeCluster = centers[images.indexOf(imgs[i])];

        int pos = 0;
        if (!simplified){
            ArrayList<double[]> coPath = new ArrayList<>();
            for (double[] point: activePath) {
                //System.out.println(point[2]);
                if (coPath.size() == 0) {
                    coPath.add(point);
                } else if (point[2] == coPath.get(coPath.size() - 1)[2]) {
                    //System.out.println("added");
                    coPath.add(point);
                } else {
                    if (coPath.get(0)[2] == 1 && coPath.size() > 1) {

                    } else if (coPath.get(0)[2] == 2 && coPath.size() > 1) {
                        out.println("<path d=\"");
                        drawContour(out, coPath, activeCluster, simplified, r);
                        out.println("stroke = \"black\" stroke-width=\"" + ((r/4)+1) + "\"");
                        out.println("/>");
                        pos++;
                    }
                    coPath.clear();
                    coPath.add(point);
                }
            }
        } else {
            ArrayList<double[]> coPath = new ArrayList<>();
            out.println("<path d=\"");
            for (double[] point: activePath) {
                //System.out.println(point[2]);
                if (coPath.size() == 0) {
                    coPath.add(point);
                } else if (point[2] == coPath.get(coPath.size() - 1)[2]) {
                    //System.out.println("added");
                    coPath.add(point);
                } else {
                    if (coPath.get(0)[2] == 2 && coPath.size() > 2) {
                        drawContour(out, coPath, activeCluster, simplified, r);
                    }
                    coPath.clear();
                    coPath.add(point);
                }
            }
            out.println("\"");
            out.println("stroke = \"black\" stroke-width=\"" + ((r/4)+1) + "\"");
            out.println("fill = \"" + toRGBCode(Color.color(activeCluster[0], activeCluster[1], activeCluster[2])) + "\"");
            out.println("/>");
            pos++;
        }
        return pos;
    }

    public static void drawContour(PrintWriter out, ArrayList<double[]> contour, float[] activeCluster, boolean simplified, int r){
        out.println("M " + contour.get(0)[0] + "," + contour.get(0)[1] + " L ");

        StringBuilder line = new StringBuilder();

        for (int i = 1; i < contour.size(); i++){
            line.append(" ").append(contour.get(i)[0]).append(",").append(contour.get(i)[1]);
        }
        out.println(line);

    }

    public static String toRGBCode( Color color )
    {
        return String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );
    }

}
