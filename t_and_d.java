import java.io.*;
import java.util.*;

public class t_and_d {
    public static void main(String[] args) {
        File inputFile = new File("t_and_d.csv");
        if (!inputFile.exists()) {
            System.out.println("Couldn't open file.");
            return;
        }

        Map<String, Map<String, Integer>> dataMap = new HashMap<>();
        Map<String, Integer> rowSums = new HashMap<>();
        Map<String, Integer> colSums = new HashMap<>();
        int totalSum = 0;

        boolean isFirstLine = true;

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                if (isFirstLine) {
                    isFirstLine = false;  
                    continue;
                }

                String[] tokens = line.split(",");
                if (tokens.length < 3) {
                    System.out.println("Invalid line: " + line);
                    continue;
                }

                String rowName = tokens[0];
                String colName = tokens[1];
                String countStr = tokens[2];

                try {
                    if (countStr.isEmpty()) {
                        throw new IllegalArgumentException("Empty count value");
                    }
                    int count = Integer.parseInt(countStr); 

                    dataMap.putIfAbsent(rowName, new HashMap<>());
                    dataMap.get(rowName).merge(colName, count, Integer::sum); 
                    rowSums.merge(rowName, count, Integer::sum);
                    colSums.merge(colName, count, Integer::sum);
                    totalSum += count;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid count value: " + countStr);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter("t_and_d_output.csv"))) {
            pw.println("Row,Column,Count");
            for (String row : rowSums.keySet()) {
                for (String col : colSums.keySet()) {
                    pw.println(row + "," + col + "," + dataMap.getOrDefault(row, new HashMap<>()).getOrDefault(col, 0));
                }
            }

            pw.println("\nRow Sums");
            pw.println("Row,Sum");
            for (Map.Entry<String, Integer> rowEntry : rowSums.entrySet()) {
                pw.println(rowEntry.getKey() + "," + rowEntry.getValue());
            }

            pw.println("\nColumn Sums");
            pw.println("Column,Sum");
            for (Map.Entry<String, Integer> colEntry : colSums.entrySet()) {
                pw.println(colEntry.getKey() + "," + colEntry.getValue());
            }

            pw.println("\nTotal Sum");
            pw.println("Total Sum," + totalSum);
        } catch (IOException e) {
            System.out.println("Couldn't open output file.");
            e.printStackTrace();
        }

        System.out.println("Output written to t_and_d_output.csv");
    }
}
