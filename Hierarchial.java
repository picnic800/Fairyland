

import java.io.*;
import java.util.*;

public class Hierarchial {
    public static void main(String[] args) {
        String inputFileName = "single_linkage_input.csv";
        String outputLogFileName = "testoutput.csv";
        List<Double> voltages = new ArrayList<>();
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose Linkage Method:");
        System.out.println("1. Single Linkage");
        System.out.println("2. Complete Linkage");
        System.out.println("3. Average Linkage");
        int choice = scanner.nextInt();

        try (BufferedReader inputFile = new BufferedReader(new FileReader(inputFileName));
             BufferedWriter logFile = new BufferedWriter(new FileWriter(outputLogFileName))) {
            
            String line;
            inputFile.readLine();
            
            while ((line = inputFile.readLine()) != null) {
                String[] fields = line.split(",");
                for (String field : fields) {
                    voltages.add(Double.parseDouble(field));
                }
            }

            List<List<Double>> clusters = new ArrayList<>();
            for (double v : voltages) {
                clusters.add(new ArrayList<>(Collections.singletonList(v)));
            }

            logFile.write("Initial clusters:\n");
            for (List<Double> cluster : clusters) {
                logFile.write("{ ");
                for (double v : cluster) {
                    logFile.write(v + " ");
                }
                logFile.write("}, ");
            }
            logFile.write("\n\n");

            while (clusters.size() > 1) {
                double minDist = Double.MAX_VALUE;
                int idx1 = -1, idx2 = -1;

                logFile.write("Distance Matrix:\n");

                for (int i = 0; i < clusters.size(); i++) {
                    for (int j = 0; j < clusters.size(); j++) {
                        if (i == j) {
                            logFile.write("0 ");
                        } else {
                            double dist = 0;
                            switch (choice) {
                                case 1:
                                    dist = singleLinkage(clusters.get(i), clusters.get(j));
                                    break;
                                case 2:
                                    dist = completeLinkage(clusters.get(i), clusters.get(j));
                                    break;
                                case 3:
                                    dist = averageLinkage(clusters.get(i), clusters.get(j));
                                    break;
                                default:
                                    System.out.println("Invalid choice. Exiting.");
                                    return;
                            }
                            logFile.write(dist + " ");

                            if (dist < minDist) {
                                minDist = dist;
                                idx1 = i;
                                idx2 = j;
                            }
                        }
                    }
                    logFile.write("\n");
                }
                logFile.write("\n");

                logFile.write("Merging clusters with minimum distance: " + minDist + "\n");

                clusters.get(idx1).addAll(clusters.get(idx2));
                clusters.remove(idx2);

                logFile.write("Clusters after merge:\n");
                for (List<Double> cluster : clusters) {
                    logFile.write("{ ");
                    for (double v : cluster) {
                        logFile.write(v + " ");
                    }
                    logFile.write("}, ");
                }
                logFile.write("\n\n");
            }

            logFile.write("Final Cluster:\n");
            for (double v : clusters.get(0)) {
                logFile.write(v + " ");
            }
            logFile.write("\n");
            
        } catch (IOException e) {
            System.err.println("Error reading or writing file: " + e.getMessage());
        }
    }

    public static double singleLinkage(List<Double> c1, List<Double> c2) {
        double minDist = Double.MAX_VALUE;
        for (double v1 : c1) {
            for (double v2 : c2) {
                double dist = Math.abs(v1 - v2);
                if (dist < minDist) {
                    minDist = dist;
                }
            }
        }
        return minDist;
    }

    public static double completeLinkage(List<Double> c1, List<Double> c2) {
        double maxDist = Double.MIN_VALUE;
        for (double v1 : c1) {
            for (double v2 : c2) {
                double dist = Math.abs(v1 - v2);
                if (dist > maxDist) {
                    maxDist = dist;
                }
            }
        }
        return maxDist;
    }

    public static double averageLinkage(List<Double> c1, List<Double> c2) {
        double totalDist = 0.0;
        int count = 0;
        for (double v1 : c1) {
            for (double v2 : c2) {
                totalDist += Math.abs(v1 - v2);
                count++;
            }
        }
        return totalDist / count;
    }
}
