package main;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class ExportSVG {

    public static void exportSVG(ArrayList<ArrayList<double[]>> allPaths, String path, String prefix, int r, int resX, int resY, float[][] centers, boolean simplified, ArrayList<Image> images, Image[] imgs, int minArea, float stroke){
        System.out.println("Starting svg export");
        String p = path + "\\" + prefix.substring(0, prefix.lastIndexOf(".")) + ".svg";
        try (PrintWriter out = new PrintWriter(p)) {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
            out.println("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"" + resX + "\" height=\"" + resY + "\">");

            int pos = 0;
            int pathCount = 0;
            int fullPointCount = 0;
            int pointsLeft = 0;
            for (int i = 0; i < allPaths.size(); i++){
               int[] ret =  writeCluster(out, allPaths, r, centers, simplified, i, images, imgs, minArea, stroke);
               pos += ret[0];
               pathCount += ret[3];
               fullPointCount += ret[1];
               pointsLeft += ret[2];
            }
            System.out.println("Points reduced: " + pos + "; All points: " + fullPointCount + "; Points left: " + pointsLeft + "; Pathcount: " + pathCount);
            out.println("</svg>");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Svg exported");
    }

    public static int[] writeCluster(PrintWriter out, ArrayList<ArrayList<double[]>> allPaths, int r, float[][] centers, boolean simplified, int i, ArrayList<Image> images, Image[] imgs, float simpleArea, float stroke){
        ArrayList<double[]> activePath = allPaths.get(i);
        float[] activeCluster = centers[images.indexOf(imgs[i])];

        int pos = 0;
        int pathCount = 0;
        int fullPointCount = 0;
        int pointsLeft = 0;
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
                        out.println("stroke = \"black\" stroke-width=\"" + (stroke) + "\"");
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
                    if ( point[3]> simpleArea) {
                        coPath.add(point);
                    }
                } else if (point[2] == coPath.get(coPath.size() - 1)[2]) {
                    if ( point[3]> simpleArea) {
                        coPath.add(point);
                    }
                } else {
                    if (coPath.get(0)[2] == 2 && coPath.size() > 2) {
                        pos += coPath.size();
                        fullPointCount += coPath.size();
                        coPath = RamerDouglasPeucker.douglasPeucker(coPath, Math.max(stroke, 2));
                        pos -= coPath.size();
                        pointsLeft += coPath.size();
                        drawContour(out, coPath, activeCluster, simplified, r);
                    }
                    coPath.clear();
                    if ( point[3]> simpleArea) {
                        coPath.add(point);
                    }
                }
            }
            out.println("\"");
            out.println("stroke = \"black\" stroke-width=\"" + (stroke) + "\"");
            out.println("fill = \"" + toRGBCode(Color.color(activeCluster[0], activeCluster[1], activeCluster[2])) + "\"");
            out.println("/>");
            pathCount++;
        }
        return new int[] {pos, fullPointCount, pointsLeft, pathCount};
    }

    public static void drawContour(PrintWriter out, ArrayList<double[]> contour, float[] activeCluster, boolean simplified, int r){
        out.println("M " + contour.get(0)[0] + "," + contour.get(0)[1] + " L ");

        StringBuilder line = new StringBuilder();

        for (int i = 1; i < contour.size(); i++){
            line.append(" ").append(contour.get(i)[0]).append(",").append(contour.get(i)[1]);
        }
        out.println(line);

    }

    public static void smoothPath(ArrayList<double[]> contour, float strength, int reps){
        if (contour.size() > 5){
            ArrayList<double[]> replace = new ArrayList<>();
            float compensate = 0;
            int count = 0;
            for (int j = 0; j < reps; j++) {
                replace.add(contour.get(0));
                for (int i = 1; i < contour.size() - 1; i++) {
                    double[] nPoint = Arrays.copyOf(contour.get(i), contour.get(i).length);
                    nPoint[0] = contour.get(i)[0] + ((contour.get(i - 1)[0] + contour.get(i + 1)[0] - (2 * contour.get(i)[0])) / (2 + strength));
                    replace.add(nPoint);
                }
                replace.add(contour.get(contour.size()-1));
                contour.clear();
                contour.addAll(replace);
                replace.clear();
            }

        }
    }

    public static String toRGBCode( Color color )
    {
        return String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );
    }





}
