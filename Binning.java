import java.io.*;
import java.util.*;

public class Binning {
    
    public static void readCSV(String filename, List<Integer> data) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                for (String value : values) {
                    data.add(Integer.parseInt(value));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeCSV(String filename, List<List<Integer>> binnedData, String method) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("Binning Method: " + method);
            for (List<Integer> bin : binnedData) {
                for (int i = 0; i < bin.size(); i++) {
                    pw.print(bin.get(i));
                    if (i != bin.size() - 1) {
                        pw.print(",");
                    }
                }
                pw.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void binning_Mean(List<Integer> data, int binSize, List<List<Integer>> binnedData) {
        int n = data.size();
        for (int i = 0; i < n; i += binSize) {
            List<Integer> bin = new ArrayList<>();
            int sum = 0;
            for (int j = i; j < Math.min(i + binSize, n); j++) {
                bin.add(data.get(j));
                sum += data.get(j);
            }
            int meanValue = sum / bin.size();
            for (int j = 0; j < bin.size(); j++) {
                bin.set(j, meanValue);
            }
            binnedData.add(bin);
        }
    }

    public static void binning_Boundary(List<Integer> data, int binSize, List<List<Integer>> binnedData) {
        int n = data.size();
        for (int i = 0; i < n; i += binSize) {
            List<Integer> bin = new ArrayList<>();
            for (int j = i; j < Math.min(i + binSize, n); j++) {
                bin.add(data.get(j));
            }
            int minValue = bin.get(0);
            int maxValue = bin.get(bin.size() - 1);
            for (int j = 0; j < bin.size(); j++) {
                int value = bin.get(j);
                bin.set(j, (value - minValue < maxValue - value) ? minValue : maxValue);
            }
            binnedData.add(bin);
        }
    }

    public static void main(String[] args) {
        List<Integer> data = new ArrayList<>();
        List<List<Integer>> binnedDataMean = new ArrayList<>();
        List<List<Integer>> binnedDataBoundary = new ArrayList<>();

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter binning size: ");
        int binSize = sc.nextInt();

        readCSV("binning.csv", data);

        Collections.sort(data);

        binning_Mean(data, binSize, binnedDataMean);

        binning_Boundary(data, binSize, binnedDataBoundary);

        writeCSV("binning_mean_output.csv", binnedDataMean, "Mean");
        writeCSV("binning_boundary_output.csv", binnedDataBoundary, "Boundary");

        System.out.println("Binning results have been written to the output files.");
        sc.close();
    } 
}
