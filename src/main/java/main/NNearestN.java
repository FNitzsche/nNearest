package main;

import java.util.Random;

public class NNearestN {

    public static float[][] lastClusters = null;

    public static float[][][] cluster(float[][][] pixel, int n, int maxReps, int resX, int resY, int seed, boolean hue, float space, boolean hueCluster){
        System.out.println("Start clustering: " + n + " Clusters");
        System.out.println("for " + pixel.length + "*" + pixel[0].length + " pixel");
        float[][] centers = new float[n][5];
        float[][] nextCenters = new float[n][6];
        Random ran = new Random(seed);

        init(ran, n, centers);

        if (hue){
            System.out.println("only Hue");
            pixel = hueNormalizeOrExpand(pixel, resX, resY, true);
        } else {
            pixel = hueNormalizeOrExpand(pixel, resX, resY, false);
        }


        clusterLoop(maxReps, nextCenters, n, resX, resY, space, pixel, centers, hue);

        if (hue && hueCluster){
            clusterHue(n, pixel, resX, resY, ran);
        }

        recolorLoop(resX, resY, centers, space, pixel, hue, n);


        System.out.println("finished clustering");
        lastClusters = centers;
        return pixel;
    }

    public static void init(Random ran, int n, float[][] centers){
        for (int i = 0; i < n; i++){
            centers[i][0] = ran.nextFloat();
            centers[i][1] = ran.nextFloat();
            centers[i][2] = ran.nextFloat();
            centers[i][3] = ran.nextFloat();
            centers[i][4] = ran.nextFloat();
        }
        System.out.println(centers.length + " Clusters init");
    }

    public static void clusterLoop(int maxReps, float[][] nextCenters, int n, int resX, int resY, float space, float[][][] pixel, float[][] centers, boolean hue){
        boolean changed = false;
        L:
        for (int i = 0; i < maxReps; i++){
            System.out.println("Rep " + i );
            changed = false;
            nextCenters = new float[n][6];
            for (int x = 0; x < resX; x++){
                for (int y = 0; y < resY; y++){
                    int nearest = -1;
                    float minDist = 5;
                    for (int k = 0; k < n; k++){
                        float dist = 5;
                        if (space == 0){
                            dist = colorDistance(pixel[x][y][0], pixel[x][y][1], pixel[x][y][2], centers[k][0], centers[k][1], centers[k][2]);
                        } else {
                            dist = colorSpaceDistance(pixel[x][y][0], pixel[x][y][1], pixel[x][y][2], (x/(float)resX), (y/(float)resY), centers[k][0], centers[k][1], centers[k][2], centers[k][3], centers[k][4], space);
                        }
                        if (dist < minDist){
                            nearest = k;
                            minDist = dist;
                        }
                    }
                    if (nearest != -1) {
                        nextCenters[nearest][0] += pixel[x][y][0];
                        nextCenters[nearest][1] += pixel[x][y][1];
                        nextCenters[nearest][2] += pixel[x][y][2];
                        nextCenters[nearest][3] += (x/(float)resX);
                        nextCenters[nearest][4] += (y/(float)resY);
                        nextCenters[nearest][5]++;
                        pixel[x][y][4] = nearest;
                    }
                }
            }
            for (int k = 0; k < n; k++){
                nextCenters[k][0] /= nextCenters[k][5];
                nextCenters[k][1] /= nextCenters[k][5];
                nextCenters[k][2] /= nextCenters[k][5];
                nextCenters[k][3] /= nextCenters[k][5];
                nextCenters[k][4] /= nextCenters[k][5];
                if (nextCenters[k][0] != centers[k][0] || nextCenters[k][1] != centers[k][1] || nextCenters[k][2] != centers[k][2]){
                    changed = true;
                }
                centers[k][0] = nextCenters[k][0];
                centers[k][1] = nextCenters[k][1];
                centers[k][2] = nextCenters[k][2];
                centers[k][3] = nextCenters[k][3];
                centers[k][4] = nextCenters[k][4];
            }

            if (!changed){
                break L;
            }
        }
    }

    public static void recolorLoop(int resX, int resY, float[][] centers, float space, float[][][] pixel, boolean hue, int n){
        for (int x = 0; x < resX; x++){
            for (int y = 0; y < resY; y++){
                int nearest = -1;
                float minDist = 5;
                for (int k = 0; k < n; k++){
                    float dist = 5;
                    if (space == 0){
                        dist = colorDistance(pixel[x][y][0], pixel[x][y][1], pixel[x][y][2], centers[k][0], centers[k][1], centers[k][2]);
                    } else {
                        dist = colorSpaceDistance(pixel[x][y][0], pixel[x][y][1], pixel[x][y][2], (x/(float)resX), (y/(float)resY), centers[k][0], centers[k][1], centers[k][2], centers[k][3], centers[k][4], space);
                    }
                    if (dist < minDist){
                        nearest = k;
                        minDist = dist;
                    }
                }
                if (nearest != -1) {
                    pixel[x][y][0] = centers[nearest][0];
                    pixel[x][y][1] = centers[nearest][1];
                    pixel[x][y][2] = centers[nearest][2];
                    if (hue) {
                        pixel[x][y][0] = Math.min(1, Math.max(0, pixel[x][y][0] * pixel[x][y][3]));
                        pixel[x][y][1] = Math.min(1, Math.max(0, pixel[x][y][1] * pixel[x][y][3]));
                        pixel[x][y][2] = Math.min(1, Math.max(0, pixel[x][y][2] * pixel[x][y][3]));
                    }
                }
            }
        }
    }

    public static float colorDistance(float r, float g, float b, float rC, float gC, float bC){
        return (float) Math.sqrt(Math.pow(rC-r, 2) + Math.pow(gC-g, 2) + Math.pow(bC-b, 2));
    }

    public static float colorSpaceDistance(float r, float g, float b, float x, float y, float rC, float gC, float bC, float xC, float yC, float space){
        return (float) Math.sqrt(Math.pow(rC-r, 2) + Math.pow(gC-g, 2) + Math.pow(bC-b, 2) + Math.pow(xC-x, 2)*space + Math.pow(yC-y, 2)*space);
    }

    public static float[][][] hueNormalizeOrExpand(float[][][] pixel, int resX, int resY, boolean norm){

        float[][][] nPixel = new float[resX][resY][5];
        for (int i = 0; i < resX; i ++){
            for (int j = 0; j < resY; j++){
                if (pixel[i][j][0]+pixel[i][j][1]+pixel[i][j][2] > 0) {
                    float length = (float) Math.sqrt(Math.pow(pixel[i][j][0], 2) + Math.pow(pixel[i][j][1], 2) + Math.pow(pixel[i][j][2], 2));
                    if (norm) {
                        nPixel[i][j][0] = pixel[i][j][0] / length;
                        nPixel[i][j][1] = pixel[i][j][1] / length;
                        nPixel[i][j][2] = pixel[i][j][2] / length;
                    } else {
                        nPixel[i][j][0] = pixel[i][j][0];
                        nPixel[i][j][1] = pixel[i][j][1];
                        nPixel[i][j][2] = pixel[i][j][2];
                    }
                    nPixel[i][j][3] = length;
                } else {
                    nPixel[i][j][0] = 0;
                    nPixel[i][j][1] = 0;
                    nPixel[i][j][2] = 0;
                    nPixel[i][j][3] = 0;
                }
            }
        }
        return nPixel;
    }

    public static void clusterHue(int n, float[][][] pixel, int resX, int resY, Random ran){
        int hueCount = 5;
        float[][] centers = new float[hueCount*n][1];
        float[][] nextCenters = new float[hueCount*n][2];
        for (int i = 0; i < n; i++){
            for (int j = 0; j < hueCount; j++){
                centers[(hueCount*i)+j][0] = (j/(float)hueCount)*1.5f;
                //System.out.println("Startcenters: " + centers[(hueCount*i)+j][0]);
            }
        }
        boolean changed = false;
        L:
        for (int i = 0; i < 5; i++){
            System.out.println("HueRep " + i );
            changed = false;
            nextCenters = new float[hueCount*n][2];
            for (int x = 0; x < resX; x++){
                for (int y = 0; y < resY; y++){
                    int nearest = -1;
                    float minDist = 5;
                    for (int k = 0; k < hueCount; k++){
                        float dist = Math.abs(pixel[x][y][3]-centers[(hueCount*(int)pixel[x][y][4])+k][0]);
                        if (dist < minDist){
                            nearest = k;
                            minDist = dist;
                        }
                    }
                    if (nearest != -1) {
                        nextCenters[(hueCount*(int)pixel[x][y][4])+nearest][0] += pixel[x][y][3];
                        nextCenters[(hueCount*(int)pixel[x][y][4])+nearest][1]++;
                    }
                }
            }
            for (int k = 0; k < hueCount*n; k++){
                nextCenters[k][0] /= nextCenters[k][1];
                if (nextCenters[k][0] != centers[k][0]){
                    changed = true;
                }
                centers[k][0] = nextCenters[k][0];
                //System.out.println(centers[k][0]);
            }

            if (!changed){
                break L;
            }
        }

        for (int x = 0; x < resX; x++){
            for (int y = 0; y < resY; y++){
                int nearest = -1;
                float minDist = 5;
                for (int k = 0; k < hueCount; k++){
                    float dist = Math.abs(pixel[x][y][3]-centers[(hueCount*(int)pixel[x][y][4])+k][0]);
                    if (dist < minDist){
                        nearest = k;
                        minDist = dist;
                    }
                }
                if (nearest != -1) {
                    pixel[x][y][3] = centers[(hueCount*(int)pixel[x][y][4])+nearest][0];
                }
            }
        }
    }

}
