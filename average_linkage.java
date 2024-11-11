import java.io.*;
import java.util.*;

public class average_linkage {

    public static double calculateDistance(double p1, double p2) {
        return Math.abs(p1 - p2);
    }

public static double averageLinkage(double[][] distanceMatrix, List<Integer> clusterA, List<Integer> clusterB) {
    double totalDistance = 0;
    int count = 0;

    for (int i : clusterA) {
        for (int j : clusterB) {
            if (i < distanceMatrix.length && j < distanceMatrix.length) { 
                totalDistance += distanceMatrix[i][j];
                count++;
            }
        }
    }
    
    
    return count > 0 ? totalDistance / count : 0;
}



    public static void appendMergeInfoToCSV(int step, int clusterA, int clusterB, List<List<Integer>> clusters, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write("Step " + step + " Merge: Cluster " + clusterA + " and Cluster " + clusterB + " into Cluster " + clusterA + "\n");
            writer.write("Merged points: ");
            for (int idx : clusters.get(clusterA)) {
                writer.write((char) ('a' + idx) + " ");
            }
            writer.write("\n");
        } catch (IOException e) {
            System.err.println("Unable to open file: " + filename);
        }
    }

    
    public static void appendMatrixToCSV(double[][] matrix, String filename, int step) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write("Step " + step + " Distance Matrix:\n");
            for (double[] row : matrix) {
                for (int i = 0; i < row.length; i++) {
                    writer.write(String.valueOf(row[i]));
                    if (i != row.length - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
            }
            writer.write("\n");
        } catch (IOException e) {
            System.err.println("Unable to open file: " + filename);
        }
    }

    public static void appendClusterContentsToCSV(int step, List<List<Integer>> clusters, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write("Step " + step + " Clusters:\n");
            for (int i = 0; i < clusters.size(); i++) {
                writer.write("Cluster " + i + ": ");
                for (int idx : clusters.get(i)) {
                    writer.write((char) ('a' + idx) + " ");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            System.err.println("Unable to open file: " + filename);
        }
    }

 
public static List<Double> readPointsFromCSV(String filename) {
    List<Double> points = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = br.readLine()) != null) {
            try {
                points.add(Double.parseDouble(line.trim()));
            } catch (NumberFormatException e) {
                System.err.println("Skipping non-numeric line: " + line);
            }
        }
    } catch (IOException e) {
        System.err.println("Unable to read file: " + filename);
    }
    return points;
}

    
    public static void hierarchicalClustering(List<Double> points, String outputFile) {
        int n = points.size();

        double[][] distanceMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    distanceMatrix[i][j] = calculateDistance(points.get(i), points.get(j));
                }
            }
        }

        appendMatrixToCSV(distanceMatrix, outputFile, 1);

        List<List<Integer>> clusters = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Integer> cluster = new ArrayList<>();
            cluster.add(i);
            clusters.add(cluster);
        }

        int step = 2;

        while (clusters.size() > 1) {
            double minDistance = Double.MAX_VALUE;
            int clusterA = -1;
            int clusterB = -1;

            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double distance = averageLinkage(distanceMatrix, clusters.get(i), clusters.get(j));
                    if (distance < minDistance) {
                        minDistance = distance;
                        clusterA = i;
                        clusterB = j;
                    }
                }
            }

            clusters.get(clusterA).addAll(clusters.get(clusterB));
            clusters.remove(clusterB);

            appendMergeInfoToCSV(step - 1, clusterA, clusterB, clusters, outputFile);
            appendClusterContentsToCSV(step - 1, clusters, outputFile);

            double[][] newDistanceMatrix = new double[clusters.size()][clusters.size()];
            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double distance = averageLinkage(distanceMatrix, clusters.get(i), clusters.get(j));
                    newDistanceMatrix[i][j] = distance;
                    newDistanceMatrix[j][i] = distance;
                }
            }
            distanceMatrix = newDistanceMatrix;

            appendMatrixToCSV(distanceMatrix, outputFile, step);
            step++;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            writer.write("Final Clusters:\n");
            for (int i = 0; i < clusters.size(); i++) {
                writer.write("Cluster " + i + ": ");
                for (int idx : clusters.get(i)) {
                    writer.write((char) ('a' + idx) + " ");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            System.err.println("Unable to open file: " + outputFile);
        }
    }

    public static void main(String[] args) {
        String inputFile = "average_linkage_input.csv";
        List<Double> points = readPointsFromCSV(inputFile);

        String outputFile = "average_linkage_output.csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
        } catch (IOException e) {
            System.err.println("Unable to open file: " + outputFile);
        }

        hierarchicalClustering(points, outputFile);
    }
}
