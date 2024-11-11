import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Point {
    double value;
    int clusterID; 
    boolean isCore;
    boolean isBoundary;
    boolean isNoise;

    Point(double value) {
        this.value = value;
        this.clusterID = 0;  
        this.isCore = false;
        this.isBoundary = false;
        this.isNoise = false;
    }
}

public class DensityClustering {

    public static double distance(double p1, double p2) {
        return Math.abs(p1 - p2);  
    }

    public static List<Point> readCSV(String filename) {
        List<Point> points = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                double value = Double.parseDouble(line.trim());
                points.add(new Point(value));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return points;
    }

    public static void writeCSV(String filename, List<Point> points, int numClusters) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Value,ClusterID,Type\n");
            for (Point point : points) {
                String type = point.isCore ? "Core" : (point.isBoundary ? "Boundary" : "Noise");
                writer.write(point.value + "," + point.clusterID + "," + type + "\n");
            }
            writer.write("Total Clusters: " + numClusters + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Integer> regionQuery(List<Point> points, int pIdx, double eps) {
        List<Integer> neighbors = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (distance(points.get(pIdx).value, points.get(i).value) <= eps) {
                neighbors.add(i);
            }
        }
        return neighbors;
    }

    public static void expandCluster(List<Point> points, int pIdx, int clusterID, double eps, int minPts) {
        List<Integer> seeds = regionQuery(points, pIdx, eps);
        
        if (seeds.size() < minPts) {
            points.get(pIdx).isNoise = true;  
            return;
        }
        
        points.get(pIdx).clusterID = clusterID;
        points.get(pIdx).isCore = true;
        
        for (int i = 0; i < seeds.size(); i++) {
            int idx = seeds.get(i);
            if (points.get(idx).clusterID == 0) {
                points.get(idx).clusterID = clusterID;
                List<Integer> newNeighbors = regionQuery(points, idx, eps);
                
                if (newNeighbors.size() >= minPts) {
                    points.get(idx).isCore = true;
                    seeds.addAll(newNeighbors);
                } else {
                    points.get(idx).isBoundary = true;
                }
            }
        }
    }

    public static void dbscan(List<Point> points, double eps, int minPts) {
        int clusterID = 0;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).clusterID == 0) {  
                if (regionQuery(points, i, eps).size() >= minPts) {
                    clusterID++;
                    expandCluster(points, i, clusterID, eps, minPts);
                } else {
                    points.get(i).isNoise = true;
                }
            }
        }
    }

    public static void main(String[] args) {
        String inputFile = "single_linkage_input.csv";
        String outputFile = "density_output.csv";
        double eps = 10.0;  
        int minPts = 2;     

        List<Point> points = readCSV(inputFile);
        dbscan(points, eps, minPts);

        int numClusters = 0;
        for (Point point : points) {
            if (point.isCore && point.clusterID > numClusters) {
                numClusters = point.clusterID;
            }
        }

        writeCSV(outputFile, points, numClusters);
        System.out.println("DBSCAN completed. Results saved in " + outputFile);
    }
}
