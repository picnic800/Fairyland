
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LinearRegressionCoefficient{

    public static void main(String[] args) {
        String inputFilename = "linear_coeff.csv";  
        String outputFilename = "linear_coefficient_output.csv"; 

        List<Double> x = new ArrayList<>();
        List<Double> y = new ArrayList<>();

        try {
            readCSV(inputFilename, x, y);

            double[] coefficients = linearRegression(x, y);
            double m = coefficients[0]; 
            double b = coefficients[1];

            
            writeCSV(outputFilename, m, b);

            System.out.println("Slope (m): " + m);
            System.out.println("Intercept (b): " + b);
            System.out.println("Results written to " + outputFilename + ".");

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static double[] linearRegression(List<Double> x, List<Double> y) throws IllegalArgumentException {
        if (x.size() != y.size() || x.isEmpty()) {
            throw new IllegalArgumentException("Input vectors must be of the same size and non-empty.");
        }

        double n = x.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x.get(i);
            sumY += y.get(i);
            sumXY += x.get(i) * y.get(i);
            sumX2 += x.get(i) * x.get(i);
        }

        double m = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double b = (sumY - m * sumX) / n;

        return new double[]{m, b};
    }

    public static void readCSV(String filename, List<Double> x, List<Double> y) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        
        reader.readLine();

        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");

            double tid = Double.parseDouble(values[0]);
            double dayValue = values[1].equals("Y") ? 1.0 : 0.0;

            x.add(tid);
            y.add(dayValue);
        }
        reader.close();
    }

    public static void writeCSV(String filename, double m, double b) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write("Slope,Intercept\n");
        writer.write(m + "," + b + "\n");
        writer.close();
    }
}
