import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Wap2 {

    public static double calculateMean(List<Double> values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    public static double calculateStdDev(List<Double> values, double mean) {
        double sum = 0;
        for (double value : values) {
            sum += Math.pow(value - mean, 2);
        }
        return Math.sqrt(sum / values.size());
    }

    public static double calculateGaussianProbability(double x, double mean, double stdDev) {
        return (1 / (Math.sqrt(2 * Math.PI) * stdDev)) * Math.exp(-Math.pow(x - mean, 2) / (2 * Math.pow(stdDev, 2)));
    }

    public static List<List<String>> readCSV(String filename) throws IOException {
        List<List<String>> data = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] row = line.split(",");
            data.add(Arrays.asList(row));
        }
        reader.close();
        return data;
    }

    public static void writeCSV(String filename, List<String> logData) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (String line : logData) {
            writer.write(line);
            writer.newLine();
        }
        writer.close();
    }

    public static void trainNaiveBayes(List<List<String>> data, int targetCol,
                                       Map<String, Map<Integer, Map<String, Double>>> categoricalCounts,
                                       Map<String, Map<Integer, List<Double>>> numericalStats,
                                       Map<String, Integer> classCounts) {

        for (int i = 1; i < data.size(); i++) {
            String targetClass = data.get(i).get(targetCol);
            classCounts.put(targetClass, classCounts.getOrDefault(targetClass, 0) + 1);

            for (int j = 0; j < data.get(0).size(); j++) {
                if (j == targetCol) continue; 

                String cellValue = data.get(i).get(j);
                if (cellValue.matches("-?\\d+(\\.\\d+)?")) { 
                    numericalStats.putIfAbsent(targetClass, new HashMap<>());
                    numericalStats.get(targetClass).putIfAbsent(j, new ArrayList<>());
                    numericalStats.get(targetClass).get(j).add(Double.parseDouble(cellValue));
                } else {  
                    categoricalCounts.putIfAbsent(targetClass, new HashMap<>());
                    categoricalCounts.get(targetClass).putIfAbsent(j, new HashMap<>());
                    categoricalCounts.get(targetClass).get(j).put(cellValue,
                            categoricalCounts.get(targetClass).get(j).getOrDefault(cellValue, 0.0) + 1);
                }
            }
        }
    }

   
    public static String predictNaiveBayes(List<List<String>> data, int targetCol,
                                           Map<String, Map<Integer, Map<String, Double>>> categoricalCounts,
                                           Map<String, Map<Integer, List<Double>>> numericalStats,
                                           Map<String, Integer> classCounts,
                                           List<String> instance, List<String> logData) {

        int totalSamples = data.size() - 1;
        double bestProb = -1;
        String bestClass = "";

        logData.add("Class,Prior Probability,Feature Probabilities,Total Probability");

        for (Map.Entry<String, Integer> classEntry : classCounts.entrySet()) {
            String targetClass = classEntry.getKey();
            double classProb = (double) classEntry.getValue() / totalSamples;
            double initialClassProb = classProb; 
            StringBuilder logLine = new StringBuilder(targetClass + "," + classProb);

            
            for (int j = 0; j < instance.size(); j++) {
                if (j == targetCol) continue; 

                String featureValue = instance.get(j);
                if (featureValue.matches("-?\\d+(\\.\\d+)?")) {  
                    List<Double> values = numericalStats.get(targetClass).get(j);
                    double mean = calculateMean(values);
                    double stdDev = calculateStdDev(values, mean);
                    double featureProb = calculateGaussianProbability(Double.parseDouble(featureValue), mean, stdDev);
                    classProb *= featureProb;

                    logLine.append(",").append(featureProb);
                } else {  
                    if (categoricalCounts.get(targetClass).get(j).containsKey(featureValue)) {
                        double featureProb = categoricalCounts.get(targetClass).get(j).get(featureValue) / classEntry.getValue();
                        classProb *= featureProb;

                        logLine.append(",").append(featureProb);  
                    } else {
                        double featureProb = 1.0 / (classEntry.getValue() + categoricalCounts.get(targetClass).get(j).size());
                        classProb *= featureProb;

                        logLine.append(",").append(featureProb);  
                    }
                }
            }

            logLine.append("=").append(classProb);  
            logData.add(logLine.toString());

            if (classProb > bestProb) {
                bestProb = classProb;
                bestClass = targetClass;
            }
        }

        return bestClass;
    }

    public static void main(String[] args) throws IOException {
        List<List<String>> data = readCSV("wap_input.csv");

        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the index of the target column (0-indexed): ");
        int targetCol = scanner.nextInt();

        Map<String, Map<Integer, Map<String, Double>>> categoricalCounts = new HashMap<>();
        Map<String, Map<Integer, List<Double>>> numericalStats = new HashMap<>();
        Map<String, Integer> classCounts = new HashMap<>();

        
        trainNaiveBayes(data, targetCol, categoricalCounts, numericalStats, classCounts);

        
        List<String> newInstance = new ArrayList<>(data.get(0).size());
        System.out.println("Enter the values for the new instance: ");
        for (int i = 0; i < data.get(0).size(); i++) {
            if (i == targetCol) continue;
            System.out.print(data.get(0).get(i) + ": ");
            newInstance.add(scanner.next());
        }

        
        List<String> logData = new ArrayList<>();

       String predictedClass = predictNaiveBayes(data, targetCol, categoricalCounts, numericalStats, classCounts, newInstance, logData);

        logData.add("Predicted Class: " + predictedClass);

        
        writeCSV("output.csv", logData);

        System.out.println("Predicted Class: " + predictedClass);
        System.out.println("Results and intermediate calculations written to output.csv");

        scanner.close();
    }
}
