import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PartitionBasedClustering {

    public static void main(String[] args) {
        String inputFilename = "partition_input.csv";
        String outputFilename = "partition_output.csv";

        List<Integer> dataPoints = readDataFromCSV(inputFilename);
        if (dataPoints.isEmpty()) {
            System.err.println("No data points found in the input file.");
            return;
        }

        int k = 2; 
        List<List<Integer>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusters.add(new ArrayList<>());
        }

        kMeansClustering(dataPoints, k, clusters);

        writeClustersToCSV(outputFilename, clusters);
        System.out.println("Clustering results saved to " + outputFilename);
    }

    public static double calculateDistance(int a, int b) {
        return Math.abs(a - b);
    }

    public static double calculateCentroid(List<Integer> cluster) {
        double sum = 0;
        for (int point : cluster) {
            sum += point;
        }
        return sum / cluster.size();
    }

    public static void kMeansClustering(List<Integer> dataPoints, int k, List<List<Integer>> clusters) {
        List<Double> centroids = new ArrayList<>();
        centroids.add((double) dataPoints.get(0)); 
        centroids.add((double) dataPoints.get(dataPoints.size() - 1)); 
        boolean converged = false;

        while (!converged) {
           
            for (List<Integer> cluster : clusters) {
                cluster.clear();
            }

           
            for (int point : dataPoints) {
                double minDistance = Double.MAX_VALUE;
                int closestCentroid = 0;

                for (int i = 0; i < k; i++) {
                    double distance = calculateDistance(point, centroids.get(i).intValue());
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestCentroid = i;
                    }
                }

                clusters.get(closestCentroid).add(point);
            }

            List<Double> newCentroids = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                if (!clusters.get(i).isEmpty()) {
                    newCentroids.add(calculateCentroid(clusters.get(i)));
                } else {
                    newCentroids.add(centroids.get(i)); 
                }
            }

            
            converged = true;
            for (int i = 0; i < k; i++) {
                if (!newCentroids.get(i).equals(centroids.get(i))) {
                    converged = false;
                    break;
                }
            }

            centroids = newCentroids;
        }
    }

    public static List<Integer> readDataFromCSV(String filename) {
        List<Integer> dataPoints = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                for (String value : values) {
                    value = value.trim(); 
                    if (!value.isEmpty()) { 
                        dataPoints.add(Integer.parseInt(value));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to open file: " + filename);
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format: " + e.getMessage());
        }
        return dataPoints;
    }

    public static void writeClustersToCSV(String filename, List<List<Integer>> clusters) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Cluster,Data Points\n");
            for (int i = 0; i < clusters.size(); i++) {
                for (int point : clusters.get(i)) {
                    writer.write("Cluster " + (i + 1) + "," + point + "\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to open file: " + filename);
        }
    }
}
 