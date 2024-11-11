import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.lang.Math;

public class coefficient {

    public static float correlationCoefficient(List<Integer> X, List<Integer> Y, int n) {
        int sum_X = 0, sum_Y = 0, sum_XY = 0;
        int squareSum_X = 0, squareSum_Y = 0;

        for (int i = 0; i < n; i++) {
            sum_X += X.get(i);
            sum_Y += Y.get(i);
            sum_XY += X.get(i) * Y.get(i);
            squareSum_X += X.get(i) * X.get(i);
            squareSum_Y += Y.get(i) * Y.get(i);
        }

        float corr = (float)(n * sum_XY - sum_X * sum_Y) 
                / (float)(Math.sqrt((n * squareSum_X - sum_X * sum_X) * (n * squareSum_Y - sum_Y * sum_Y)));
        return corr;
    }

    public static void readAndTransposeCSV(String filename, List<List<Integer>> data, List<String> columnNames) throws IOException {
        List<String[]> tempData = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(filename))) {
            String line = br.readLine();
            if (line != null) {
                columnNames.addAll(Arrays.asList(line.split(","))); 
            }

            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");
                tempData.add(row); 
            }
        }

        int rows = tempData.size();
        int cols = tempData.get(0).length;

        for (int i = 1; i < cols; i++) {  
            List<Integer> column = new ArrayList<>();
            for (int j = 0; j < rows; j++) {
                String value = tempData.get(j)[i].trim(); 
                if (value.equals("Y")) {
                    column.add(1);  
                } else if (value.equals("N")) {
                    column.add(0);  
                } else {
                    try {
                        column.add(Integer.parseInt(value)); 
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping non-numeric value: " + value);
                    }
                }
            }
            data.add(column);
        }
    }

    public static void writeCSV(String filename, List<Tuple> results) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename))) {
            writer.write("Item 1,Item 2,Correlation Coefficient,Type of correlation\n");
            for (Tuple result : results) {
                writer.write(result.col1 + "," + result.col2 + "," + result.correlation + "," + result.corrType + "\n");
            }
        }
    }

    public static void printResults(List<Tuple> results) {
        System.out.println("Item 1,Item 2,Correlation Coefficient,Type of correlation");
        for (Tuple result : results) {
            System.out.println(result.col1 + "," + result.col2 + "," + result.correlation + "," + result.corrType);
        }
    }

    public static void main(String[] args) throws IOException {
        List<List<Integer>> data = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();

        
        readAndTransposeCSV("co_input.csv", data, columnNames);
        int numColumns = data.size();

        if (numColumns == 0) {
            System.out.println("No sufficient data found in co_input.csv");
            return;
        }

        List<Tuple> results = new ArrayList<>();

      
        for (int i = 0; i < numColumns; i++) {
            for (int j = i + 1; j < numColumns; j++) {
                float corr = correlationCoefficient(data.get(i), data.get(j), data.get(i).size());
                String corrType = (corr > 0) ? "Positive correlation" : (corr < 0) ? "Negative correlation" : "No correlation";
                results.add(new Tuple(columnNames.get(i + 1), columnNames.get(j + 1), corr, corrType));  // Fixed index
            }
        }

     
        printResults(results);
        writeCSV("co_output.csv", results);
        System.out.println("Correlation results written to co_output.csv");
    }

    static class Tuple {
        String col1;
        String col2;
        float correlation;
        String corrType;

        public Tuple(String col1, String col2, float correlation, String corrType) {
            this.col1 = col1;
            this.col2 = col2;
            this.correlation = correlation;
            this.corrType = corrType;
        }
    }
}
