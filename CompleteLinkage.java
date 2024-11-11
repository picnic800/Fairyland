import java.io.*;
import java.util.*;

public class CompleteLinkage {

    public static double calculateDistance(double p1, double p2) {
        return Math.abs(p1 - p2);
    }

    public static double completeLinkage(List<List<Double>> distanceMatrix, List<Integer> clusterA, List<Integer> clusterB) {
        double maxDistance = 0;
        System.out.println("Calculating complete linkage for clusters:");
        System.out.println("Cluster A: " + clusterA);
        System.out.println("Cluster B: " + clusterB);
    
        for (int i : clusterA) {
            for (int j : clusterB) {
                if (i < distanceMatrix.size() && j < distanceMatrix.size()) {
                    maxDistance = Math.max(maxDistance, distanceMatrix.get(i).get(j));
                } else {
                    System.out.println("Invalid index access: i=" + i + ", j=" + j);
                }
            }
        }
        return maxDistance;
    }
    
    public static void appendMergeInfoToCSV(int step, Pair<Integer, Integer> clustersToMerge, List<List<Integer>> clusters, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write("Step " + step + " Merge: Cluster " + clustersToMerge.getFirst() +
                    " and Cluster " + clustersToMerge.getSecond() + " into Cluster " + clustersToMerge.getFirst() + "\n");
            writer.write("Merged points: ");
            for (int idx : clusters.get(clustersToMerge.getFirst())) {
                writer.write((char) ('a' + idx) + " ");
            }
            writer.write("\n");
        } catch (IOException e) {
            System.err.println("Unable to open file: " + filename);
        }
    }

    public static void appendMatrixToCSV(List<List<Double>> matrix, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write("Distance Matrix:\n");
            for (List<Double> row : matrix) {
                for (int j = 0; j < row.size(); j++) {
                    writer.write(row.get(j).toString());
                    if (j != row.size() - 1)
                        writer.write(",");
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
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                for (String value : values) {
                    try {
                        points.add(Double.parseDouble(value.trim()));
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping non-numeric value: " + value);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename);
        }
        return points;
    }

    public static void hierarchicalClustering(List<Double> points, String outputFile) {
        int n = points.size();

        List<String> pointLabels = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            pointLabels.add(String.valueOf((char) ('a' + i)));
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

        List<List<Integer>> clusters = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Integer> cluster = new ArrayList<>();
            cluster.add(i);
            clusters.add(cluster);
        }

        int step = 1;

        while (clusters.size() > 1) {
            double minDistance = Double.POSITIVE_INFINITY;
            Pair<Integer, Integer> clustersToMerge = new Pair<>(-1, -1);

            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double distance = completeLinkage(distanceMatrix, clusters.get(i), clusters.get(j));
                    if (distance < minDistance) {
                        minDistance = distance;
                        clustersToMerge = new Pair<>(i, j);
                    }
                }
            }

            int clusterA = clustersToMerge.getFirst();
            int clusterB = clustersToMerge.getSecond();
            clusters.get(clusterA).addAll(clusters.get(clusterB));
            clusters.remove(clusterB);

            appendMergeInfoToCSV(step, clustersToMerge, clusters, outputFile);
            appendClusterContentsToCSV(step, clusters, outputFile);

            List<List<Double>> newDistanceMatrix = new ArrayList<>(clusters.size());
            for (int i = 0; i < clusters.size(); i++) {
                List<Double> newRow = new ArrayList<>(Collections.nCopies(clusters.size(), 0.0));
                newDistanceMatrix.add(newRow);
            }
            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double distance = completeLinkage(distanceMatrix, clusters.get(i), clusters.get(j));
                    newDistanceMatrix.get(i).set(j, distance);
                    newDistanceMatrix.get(j).set(i, distance);
                }
            }
            distanceMatrix = newDistanceMatrix;

            appendMatrixToCSV(distanceMatrix, outputFile);

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
        String inputFile = "density_clustering.csv";
        List<Double> points = readPointsFromCSV(inputFile);

        String outputFile = "density_clustering_output.csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
        } catch (IOException e) {
            System.err.println("Error initializing output file: " + outputFile);
        }

        hierarchicalClustering(points, outputFile);
    }

    public static class Pair<F, S> {
        private final F first;
        private final S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public F getFirst() {
            return first;
        }

        public S getSecond() {
            return second;
        }
    }
}
