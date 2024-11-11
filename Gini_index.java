
import java.io.*;
import java.util.*;

public class Gini_index {

    private static double calculateEntropy(double yes, double no) {
        double total = yes + no;
        if (total == 0) return 0; 
        double pYes = yes / total;
        double pNo = no / total;

        double entropy = 0;
        if (pYes > 0) entropy -= pYes * (Math.log(pYes) / Math.log(2));
        if (pNo > 0) entropy -= pNo * (Math.log(pNo) / Math.log(2));

        return entropy;
    }

    private static double calculateGini(double yes, double no) {
        double total = yes + no;
        if (total == 0) return 0; 
        double pYes = yes / total;
        double pNo = no / total;

        return 1 - (pYes * pYes + pNo * pNo);
    }

    private static void readCSV(String filename, List<List<String>> data) {
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

    private static void writeCSV(String filename, List<String> logData) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (String line : logData) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error opening file for writing: " + filename);
        }
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void calculateForNumerical(List<List<String>> data, int featureCol, int targetCol,
                                               Map<String, Double> targetCount, List<String> logData,
                                               double parentEntropy, int totalSamples, double[] bestFinalGini) {
        Set<Double> uniqueValues = new TreeSet<>();
        for (int i = 1; i < data.size(); i++) {
            uniqueValues.add(Double.parseDouble(data.get(i).get(featureCol)));
        }

        double bestInfoGain = -1;
        double bestSplit = 0;
        for (double splitPoint : uniqueValues) {
            double leftYes = 0, leftNo = 0, rightYes = 0, rightNo = 0;

            for (int i = 1; i < data.size(); i++) {
                double featureValue = Double.parseDouble(data.get(i).get(featureCol));
                String targetValue = data.get(i).get(targetCol);

                if (featureValue <= splitPoint) {
                    if ("Yes".equals(targetValue)) leftYes++;
                    else leftNo++;
                } else {
                    if ("Yes".equals(targetValue)) rightYes++;
                    else rightNo++;
                }
            }

            double leftTotal = leftYes + leftNo;
            double rightTotal = rightYes + rightNo;
            double weightedEntropy = ((leftTotal / totalSamples) * calculateEntropy(leftYes, leftNo)) +
                                     ((rightTotal / totalSamples) * calculateEntropy(rightYes, rightNo));
            double weightedGini = ((leftTotal / totalSamples) * calculateGini(leftYes, leftNo)) +
                                  ((rightTotal / totalSamples) * calculateGini(rightYes, rightNo));

            double infoGain = parentEntropy - weightedEntropy;

            String logEntry = "Split Point: " + splitPoint + ", Weighted Entropy: " + weightedEntropy +
                              ", Gini Index: " + weightedGini + ", Information Gain: " + infoGain;
            logData.add(logEntry);

            if (infoGain > bestInfoGain) {
                bestInfoGain = infoGain;
                bestSplit = splitPoint;
                bestFinalGini[0] = weightedGini; 
            }
        }

        logData.add("Best Split Point: " + bestSplit + " with Information Gain: " + bestInfoGain);
    }

    private static void calculateForCategorical(List<List<String>> data, int featureCol, int targetCol,
                                                 Map<String, Double> targetCount, List<String> logData,
                                                 double parentEntropy, int totalSamples, double[] bestFinalGini) {
        Map<String, Map<String, Double>> featureMap = new HashMap<>(); 

        for (int i = 1; i < data.size(); i++) {
            String featureValue = data.get(i).get(featureCol);
            String targetValue = data.get(i).get(targetCol);

            featureMap.computeIfAbsent(featureValue, k -> new HashMap<>()).merge(targetValue, 1.0, Double::sum);
        }

        double weightedEntropy = 0;
        double weightedGiniIndex = 0;
        for (Map.Entry<String, Map<String, Double>> featureEntry : featureMap.entrySet()) {
            double featureTotal = 0;
            double featureEntropy = 0;
            double featureGini = 0;

            for (double count : featureEntry.getValue().values()) {
                featureTotal += count;
            }

            for (double count : featureEntry.getValue().values()) {
                double p = count / featureTotal;
                if (p > 0) featureEntropy -= p * (Math.log(p) / Math.log(2));
            }

            for (double count : featureEntry.getValue().values()) {
                double p = count / featureTotal;
                featureGini += p * p;
            }
            featureGini = 1 - featureGini;

            weightedEntropy += (featureTotal / totalSamples) * featureEntropy;
            weightedGiniIndex += (featureTotal / totalSamples) * featureGini;

            logData.add("Feature: " + featureEntry.getKey() + " | Weighted Entropy: " + featureEntropy + " | Gini Index: " + featureGini);
        }

        double infoGain = parentEntropy - weightedEntropy;
        bestFinalGini[0] = weightedGiniIndex; 
        logData.add("Information Gain for Selected Feature: " + infoGain);
    }

    public static void main(String[] args) {
        List<List<String>> data = new ArrayList<>();
        String inputFile = "gini_input.csv";
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
            targetCount.merge(data.get(i).get(targetCol), 1.0, Double::sum);
        }

        double parentEntropy = 0;
        for (double count : targetCount.values()) {
            double p = count / totalSamples;
            if (p > 0) parentEntropy -= p * (Math.log(p) / Math.log(2));
        }

        List<String> logData = new ArrayList<>();
        logData.add("Parent Entropy: " + parentEntropy);

        double[] bestFinalGini = new double[1];

        if (isNumeric(data.get(1).get(featureCol))) {
            logData.add("Numerical Feature Selected");
            calculateForNumerical(data, featureCol, targetCol, targetCount, logData, parentEntropy, totalSamples, bestFinalGini);
        } else {
            logData.add("Categorical Feature Selected");
            calculateForCategorical(data, featureCol, targetCol, targetCount, logData, parentEntropy, totalSamples, bestFinalGini);
        }

        logData.add("Final Gini Index after Best Split: " + bestFinalGini[0]);

        String outputFile = "gini_output.csv";
        writeCSV(outputFile, logData);

        System.out.println("Results written to " + outputFile);
    }
}
