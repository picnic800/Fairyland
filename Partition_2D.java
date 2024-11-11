import java.io.*;
import java.util.*;

public class Partition_2D {

    public static void main(String[] args) {
        String inputFilename = "partition2D_input.csv";
        String outputFilename = "Partition2D_cluster_output.csv";

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of clusters (k): ");
        int k = scanner.nextInt();  

        List<Point> points = readPointsFromCSV(inputFilename);
        if (points.isEmpty()) {
            System.out.println("No data points found in the input file.");
            return;
        }

       
        List<Point> centroids = initializeCentroids(points, k);
        List<Integer> clusters;
        List<Point> newCentroids;
        int iterations = 0;
        final double tolerance = 0.001;

        do {
            clusters = assignClusters(points, centroids);
            newCentroids = recomputeCentroids(points, clusters, k);

            if (hasConverged(centroids, newCentroids, tolerance)) {
                break;
            }

            centroids = newCentroids;
            iterations++;

        } while (true);

        writeOutputToCSV(outputFilename, points, clusters, centroids);
        System.out.println("K-means clustering completed in " + iterations + " iterations.");
    }

    public static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    public static List<Point> readPointsFromCSV(String filename) {
        List<Point> points = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine(); 
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                points.add(new Point(Integer.parseInt(values[1]), Integer.parseInt(values[2])));
            }
        } catch (IOException e) {
            System.err.println("Unable to open file: " + filename);
        }
        return points;
    }

    public static List<Point> initializeCentroids(List<Point> points, int k) {
        List<Point> centroids = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            centroids.add(points.get(i));  
        }
        return centroids;
    }

    public static List<Integer> assignClusters(List<Point> points, List<Point> centroids) {
        List<Integer> clusters = new ArrayList<>(Collections.nCopies(points.size(), 0));

        for (int i = 0; i < points.size(); i++) {
            double minDist = Double.MAX_VALUE;
            int clusterIdx = 0;

            for (int j = 0; j < centroids.size(); j++) {
                double dist = distance(points.get(i).x, points.get(i).y, centroids.get(j).x, centroids.get(j).y);
                if (dist < minDist) {
                    minDist = dist;
                    clusterIdx = j;
                }
            }
            clusters.set(i, clusterIdx);
        }

        return clusters;
    }

    public static List<Point> recomputeCentroids(List<Point> points, List<Integer> clusters, int k) {
        List<Point> newCentroids = new ArrayList<>(Collections.nCopies(k, new Point(0, 0)));
        int[] count = new int[k];

        for (int i = 0; i < points.size(); i++) {
            int clusterIdx = clusters.get(i);
            newCentroids.get(clusterIdx).x += points.get(i).x;
            newCentroids.get(clusterIdx).y += points.get(i).y;
            count[clusterIdx]++;
        }

        for (int i = 0; i < k; i++) {
            if (count[i] > 0) {
                newCentroids.set(i, new Point(newCentroids.get(i).x / count[i], newCentroids.get(i).y / count[i]));
            }
        }

        return newCentroids;
    }

    public static boolean hasConverged(List<Point> centroids, List<Point> newCentroids, double tolerance) {
        for (int i = 0; i < centroids.size(); i++) {
            if (distance(centroids.get(i).x, centroids.get(i).y, newCentroids.get(i).x, newCentroids.get(i).y) > tolerance) {
                return false;
            }
        }
        return true;
    }

    public static void writeOutputToCSV(String filename, List<Point> points, List<Integer> clusters, List<Point> centroids) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
           
            writer.write("Distance Matrix\n,");
            for (int i = 0; i < points.size(); i++) {
                writer.write("p" + (i + 1) + ",");
            }
            writer.write("\n");

            for (int i = 0; i < points.size(); i++) {
                writer.write("p" + (i + 1) + ",");
                for (int j = 0; j < points.size(); j++) {
                    if (i == j) {
                        writer.write("0,");
                    } else {
                        double dist = distance(points.get(i).x, points.get(i).y, points.get(j).x, points.get(j).y);
                        writer.write(dist + ",");
                    }
                }
                writer.write("\n");
            }

          
            writer.write("\nPoint,Cluster\n");
            for (int i = 0; i < points.size(); i++) {
                writer.write("p" + (i + 1) + "," + (clusters.get(i) + 1) + "\n");  
            }

           
            writer.write("\nFinal Centroids\n");
            for (int i = 0; i < centroids.size(); i++) {
                writer.write("Cluster " + (i + 1) + ": (" + centroids.get(i).x + ", " + centroids.get(i).y + ")\n");
            }
        } catch (IOException e) {
            System.err.println("Unable to open file: " + filename);
        }
    }
}
