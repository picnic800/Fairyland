import java.io.*;
import java.util.*;

public class Entropy {

    public static double calculateEntropy(double yes, double no) {
        double total = yes + no;
        if (total == 0) return 0;
        double pYes = yes / total;
        double pNo = no / total;

        double entropy = 0;
        if (pYes > 0) entropy -= pYes * Math.log(pYes) / Math.log(2);
        if (pNo > 0) entropy -= pNo * Math.log(pNo) / Math.log(2);

        return entropy;
    }

    public static void readCSV(String filename, List<List<String>> data) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                data.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            System.err.println("Error opening file: " + filename);
        }
    }

    public static void writeCSV(String filename, double parentEntropy, double weightedEntropy, double infoGain) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write("Parent Entropy," + parentEntropy + "\n");
            bw.write("Weighted Entropy (Selected Feature)," + weightedEntropy + "\n");
            bw.write("Information Gain (Selected Feature)," + infoGain + "\n");
        } catch (IOException e) {
            System.err.println("Error opening file for writing: " + filename);
        }
    }

    public static void main(String[] args) {
        List<List<String>> data = new ArrayList<>();
        String inputFile = "data_input.csv";  
        readCSV(inputFile, data);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the index of the target column (0-indexed): ");
        int targetCol = scanner.nextInt();
        System.out.print("Enter the index of the feature column (0-indexed): ");
        int featureCol = scanner.nextInt();

        if (data.size() < 2 || targetCol >= data.get(0).size() || featureCol >= data.get(0).size()) {
            System.err.println("Invalid input or no data found in the CSV file.");
            return;
        }

        Map<String, Double> targetCount = new HashMap<>();
        int totalSamples = data.size() - 1;

        for (int i = 1; i < data.size(); i++) {
            targetCount.put(data.get(i).get(targetCol), targetCount.getOrDefault(data.get(i).get(targetCol), 0.0) + 1);
        }

        double parentEntropy = 0;
        for (double count : targetCount.values()) {
            double p = count / totalSamples;
            if (p > 0) parentEntropy -= p * Math.log(p) / Math.log(2);
        }

        System.out.println("Calculating Parent Entropy");
        System.out.println("Parent Entropy: " + parentEntropy);

        Map<String, Map<String, Double>> featureMap = new HashMap<>();

        for (int i = 1; i < data.size(); i++) {
            String featureValue = data.get(i).get(featureCol);
            String targetValue = data.get(i).get(targetCol);
            featureMap.putIfAbsent(featureValue, new HashMap<>());
            featureMap.get(featureValue).put(targetValue, featureMap.get(featureValue).getOrDefault(targetValue, 0.0) + 1);
        }

        double weightedEntropy = 0;
        for (Map.Entry<String, Map<String, Double>> featureEntry : featureMap.entrySet()) {
            double featureTotal = 0;
            double featureEntropy = 0;

            for (double count : featureEntry.getValue().values()) {
                featureTotal += count;
            }
            for (double count : featureEntry.getValue().values()) {
                double p = count / featureTotal;
                if (p > 0) featureEntropy -= p * Math.log(p) / Math.log(2);
            }

            weightedEntropy += (featureTotal / totalSamples) * featureEntropy;
            System.out.println("Feature: " + featureEntry.getKey() + " | Weighted Entropy: " + featureEntropy);
        }
        
        double infoGain = parentEntropy - weightedEntropy;

        String outputFile = "entropy_output.csv";  
        writeCSV(outputFile, parentEntropy, weightedEntropy, infoGain);

        System.out.println("Final Results:");
        System.out.println("Parent Entropy: " + parentEntropy);
        System.out.println("Weighted Entropy for Selected Feature: " + weightedEntropy);
        System.out.println("Information Gain for Selected Feature: " + infoGain);

        System.out.println("Results written to " + outputFile);
        
        scanner.close();
    }
}
