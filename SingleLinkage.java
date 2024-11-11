import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SingleLinkage {

    public static double calculateDistance(double p1, double p2) {
        return Math.abs(p1 - p2);
    }

    public static double singleLinkage(List<List<Double>> distanceMatrix, List<Integer> clusterA, List<Integer> clusterB) {
        double minDistance = Double.POSITIVE_INFINITY;

        System.out.println("Cluster A: " + clusterA);
        System.out.println("Cluster B: " + clusterB);
        System.out.println("Distance Matrix Size: " + distanceMatrix.size());

        for (int i : clusterA) {
            for (int j : clusterB) {
                if (i < 0 || i >= distanceMatrix.size() || j < 0 || j >= distanceMatrix.get(i).size()) {
                    System.out.println("Invalid index access: i=" + i + ", j=" + j);
                    continue; 
                }
                minDistance = Math.min(minDistance, distanceMatrix.get(i).get(j));
            }
        }
        return minDistance;
    }

    public static void appendMergeInfoToCSV(int step, int clusterA, int clusterB, List<List<Integer>> clusters, String filename) {
        try (FileWriter fileWriter = new FileWriter(filename, true)) {
            fileWriter.write("Step " + step + " Merge: Cluster " + clusterA + " and Cluster " + clusterB + "\n");
            fileWriter.write("Merged points: ");
            for (int idx : clusters.get(clusterA)) {
                fileWriter.write((char) ('a' + idx) + " ");
            }
            fileWriter.write("\n");
        } catch (IOException e) {
            System.err.println("Unable to open file: " + filename);
        }
    }

    public static void appendMatrixToCSV(List<List<Double>> matrix, String filename) {
        try (FileWriter fileWriter = new FileWriter(filename, true)) {
            fileWriter.write("Distance Matrix:\n");
            for (List<Double> row : matrix) {
                for (int j = 0; j < row.size(); j++) {
                    fileWriter.write(row.get(j).toString());
                    if (j != row.size() - 1) {
                        fileWriter.write(",");
                    }
                }
                fileWriter.write("\n");
            }
            fileWriter.write("\n");
        } catch (IOException e) {
            System.err.println("Unable to open file: " + filename);
        }
    }

    public static void appendClusterContentsToCSV(int step, List<List<Integer>> clusters, String filename) {
        try (FileWriter fileWriter = new FileWriter(filename, true)) {
            fileWriter.write("Step " + step + " Clusters:\n");
            for (int i = 0; i < clusters.size(); i++) {
                fileWriter.write("Cluster " + i + ": ");
                for (int idx : clusters.get(i)) {
                    fileWriter.write((char) ('a' + idx) + " ");
                }
                fileWriter.write("\n");
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
                String[] values = line.split(",");
                for (String value : values) {
                    points.add(Double.parseDouble(value.trim()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename);
        }
        return points;
    }

    public static void hierarchicalClustering(List<Double> points, String outputFile) {
        int n = points.size();

        List<List<Integer>> clusters = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Integer> cluster = new ArrayList<>();
            cluster.add(i);
            clusters.add(cluster);
        }

        List<List<Double>> distanceMatrix = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Double> row = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    row.add(calculateDistance(points.get(i), points.get(j)));
                } else {
                    row.add(0.0);
                }
            }
            distanceMatrix.add(row);
        }

        appendMatrixToCSV(distanceMatrix, outputFile);
        int step = 1;

        while (clusters.size() > 1) {
            double minDistance = Double.POSITIVE_INFINITY;
            int clusterA = -1, clusterB = -1;

            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double distance = singleLinkage(distanceMatrix, clusters.get(i), clusters.get(j));
                    if (distance < minDistance) {
                        minDistance = distance;
                        clusterA = i;
                        clusterB = j;
                    }
                }
            }

            clusters.get(clusterA).addAll(clusters.get(clusterB));
            clusters.remove(clusterB);

            appendMergeInfoToCSV(step, clusterA, clusterB, clusters, outputFile);
            appendClusterContentsToCSV(step, clusters, outputFile);

            List<List<Double>> newDistanceMatrix = new ArrayList<>();
            for (int i = 0; i < clusters.size(); i++) {
                List<Double> row = new ArrayList<>();
                for (int j = 0; j < clusters.size(); j++) {
                    if (i != j) {
                        double distance = singleLinkage(distanceMatrix, clusters.get(i), clusters.get(j));
                        row.add(distance);
                    } else {
                        row.add(0.0);
                    }
                }
                newDistanceMatrix.add(row);
            }
            distanceMatrix = newDistanceMatrix;

            appendMatrixToCSV(distanceMatrix, outputFile);
            step++;
        }

        try (FileWriter fileWriter = new FileWriter(outputFile, true)) {
            fileWriter.write("Final Clusters:\n");
            for (int i = 0; i < clusters.size(); i++) {
                fileWriter.write("Cluster " + i + ": ");
                for (int idx : clusters.get(i)) {
                    fileWriter.write((char) ('a' + idx) + " ");
                }
                fileWriter.write("\n");
            }
        } catch (IOException e) {
            System.err.println("Unable to open file: " + outputFile);
        }
    }

    public static void main(String[] args) {
        String inputFile = "single_linkage_input.csv";
        List<Double> points = readPointsFromCSV(inputFile);

        String outputFile = "single_linkage_output.csv";

        try (FileWriter fileWriter = new FileWriter(outputFile, false)) {
            fileWriter.write(""); 
        } catch (IOException e) {
            System.err.println("Unable to open file: " + outputFile);
        }

        hierarchicalClustering(points, outputFile);
    }
}
