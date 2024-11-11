import java.io.*;
import java.util.*;
import java.lang.Math;

public class ZScoreNormalization {

    public static double[] calculateMeanAndStd(List<Double> values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        double mean = sum / values.size();

        double varianceSum = 0.0;
        for (double value : values) {
            varianceSum += Math.pow(value - mean, 2);
        }
        double variance = varianceSum / values.size();
        double stdDev = Math.sqrt(variance);

        return new double[] { mean, stdDev };
    }

    public static double zScoreNormalize(double value, double mean, double stdDev) {
        return (value - mean) / stdDev;
    }

    public static void main(String[] args) {
        String inputFilename = "iris.csv";
        String outputFilename = "z_score_normalized_data.csv";

        List<String[]> data = new ArrayList<>();
        List<Double> marks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilename))) {
            String line = reader.readLine();
            if (line != null) {
                data.add(line.split(","));
            }

            
            while ((line = reader.readLine()) != null) {
                String[] row = line.split(",");
                String rollNo = row[0];
                double totalMarks = Double.parseDouble(row[1]);
                data.add(new String[] { rollNo, String.valueOf(totalMarks) });
                marks.add(totalMarks);
            }

        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }

        double[] meanAndStd = calculateMeanAndStd(marks);
        double mean = meanAndStd[0];
        double stdDev = meanAndStd[1];

        List<String[]> normalizedData = new ArrayList<>();
        normalizedData.add(data.get(0));  
        for (int i = 1; i < data.size(); i++) {
            String rollNo = data.get(i)[0];
            double totalMarks = Double.parseDouble(data.get(i)[1]);
            double normalizedMarks = zScoreNormalize(totalMarks, mean, stdDev);
            normalizedData.add(new String[] { rollNo, String.format("%.4f", normalizedMarks) });
        }

        try (FileWriter writer = new FileWriter(outputFilename)) {
            for (String[] row : normalizedData) {
                writer.append(String.join(",", row));
                writer.append("\n");
            }
            System.out.println("Z-score normalization complete. The normalized data is saved in 'z_score_normalized_data.csv'.");
        } catch (IOException e) {
            System.out.println("Error writing the file: " + e.getMessage());
        }
    }
}
