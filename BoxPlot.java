import java.io.*;
import java.util.*;

public class BoxPlot {

    public static double median(List<Double> a) {
        int size = a.size();
        if (size % 2 == 1) {
            return a.get(size / 2);
        } else {
            return (a.get((size / 2) - 1) + a.get(size / 2)) / 2.0;
        }
    }

    public static double quartile1(List<Double> v) {
        int n = v.size();
        List<Double> first = new ArrayList<>();
        for (int i = 0; i < n / 2; i++) {
            first.add(v.get(i));
        }
        return median(first);
    }

    public static double quartile3(List<Double> v) {
        int n = v.size();
        List<Double> last = new ArrayList<>();
        if (n % 2 == 0) {
            for (int i = n / 2; i < n; i++) {
                last.add(v.get(i));
            }
        } else {
            for (int i = n / 2 + 1; i < n; i++) {
                last.add(v.get(i));
            }
        }
        return median(last);
    }

    public static void main(String[] args) {
        String inputFilename = "iris.csv";
        String outputFilename = "boxplot_output.csv";

        List<Double> arr = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilename))) {
            String line;
            int i = 0;

            while ((line = br.readLine()) != null) {
                if (i == 0) {
                    i++;
                    continue; 
                }
                String[] values = line.split(",");
                double mark = Double.parseDouble(values[0]);  
                arr.add(mark);
            }

        } catch (IOException e) {
            System.out.println("Couldn't open file: " + e.getMessage());
            return;
        }

        Collections.sort(arr);
        int n = arr.size();

        try (FileWriter out = new FileWriter(outputFilename)) {
            out.write("Minimum value: ," + arr.get(0) + "\n");
            out.write("Quartile1 value: ," + quartile1(arr) + "\n");
            out.write("Median value: ," + median(arr) + "\n");
            out.write("Quartile3 value: ," + quartile3(arr) + "\n");
            out.write("Maximum value: ," + arr.get(n - 1) + "\n");

            System.out.println("Minimum value is " + arr.get(0));
            System.out.println("Q1: " + quartile1(arr));
            System.out.println("Median: " + median(arr));
            System.out.println("Q3: " + quartile3(arr));
            System.out.println("Maximum value is " + arr.get(n - 1));

        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }
}
