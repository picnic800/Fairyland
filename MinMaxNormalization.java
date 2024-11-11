import java.io.*;
import java.util.*;

public class MinMaxNormalization {
    
    public static double minMaxNormalize(double value, double minVal, double maxVal, double newMin, double newMax) {
        return ((value - minVal) / (maxVal - minVal)) * (newMax - newMin) + newMin;
    }

    public static void main(String[] args) {
        String inputFilename = "C:\\Users\\Sakshi\\DM\\DM java\\iris.csv";
        String outputFilename = "normalized_data.csv";

        List<String[]> data = new ArrayList<>();
        double minMarks = Double.MAX_VALUE;
        double maxMarks = Double.MIN_VALUE;

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

                
                if (totalMarks < minMarks) {
                    minMarks = totalMarks;
                }
                if (totalMarks > maxMarks) {
                    maxMarks = totalMarks;
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }

        
        double newMin = 0;
        double newMax = 1;

        
        List<String[]> normalizedData = new ArrayList<>();
        normalizedData.add(data.get(0));  
        for (int i = 1; i < data.size(); i++) {
            String rollNo = data.get(i)[0];
            double totalMarks = Double.parseDouble(data.get(i)[1]);
            double normalizedMarks = minMaxNormalize(totalMarks, minMarks, maxMarks, newMin, newMax);
            normalizedData.add(new String[] { rollNo, String.format("%.4f", normalizedMarks) });
        }

        
        try (FileWriter writer = new FileWriter(outputFilename)) {
            for (String[] row : normalizedData) {
                writer.append(String.join(",", row));
                writer.append("\n");
            }
            System.out.println("Normalization complete. The normalized data is saved in 'normalized_data.csv'.");
        } catch (IOException e) {
            System.out.println("Error writing the file: " + e.getMessage());
        }
    }
}
