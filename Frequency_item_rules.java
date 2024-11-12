import java.io.*;
import java.util.*;

public class Frequency_item_rules {
    private static BufferedReader fin;
    private static double minFrequency;
    private static List<Set<String>> datatable = new ArrayList<>();
    private static Set<String> products = new HashSet<>();
    private static Map<String, Integer> freq = new HashMap<>();
    private static double confidence;

    private static List<String> wordsOf(String str) {
        List<String> tmpset = new ArrayList<>();
        StringBuilder tmp = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                tmp.append(c);
            } else {
                if (tmp.length() > 0) {
                    tmpset.add(tmp.toString());
                    tmp.setLength(0);
                }
            }
        }
        if (tmp.length() > 0) {
            tmpset.add(tmp.toString());
        }
        return tmpset;
    }

    private static String combine(List<String> arr, int miss) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < arr.size(); i++) {
            if (i != miss) {
                str.append(arr.get(i)).append(" ");
            }
        }
        return str.toString().trim();
    }

    private static Set<String> cloneSet(Set<String> arr) {
        return new HashSet<>(arr);
    }

    private static Set<String> aprioriGen(Set<String> sets, int k) {
        Set<String> set2 = new HashSet<>();
        for (String s1 : sets) {
            for (String s2 : sets) {
                if (!s1.equals(s2)) {
                    List<String> v1 = wordsOf(s1);
                    List<String> v2 = wordsOf(s2);
                    boolean alleq = true;
                    for (int i = 0; i < k - 1 && alleq; i++) {
                        if (!v1.get(i).equals(v2.get(i))) {
                            alleq = false;
                        }
                    }
                    v1.add(v2.get(k - 1));
                    if (v1.get(v1.size() - 1).compareTo(v1.get(v1.size() - 2)) < 0) {
                        Collections.swap(v1, v1.size() - 1, v1.size() - 2);
                    }
                    boolean valid = true;
                    for (int i = 0; i < v1.size() && valid; i++) {
                        if (!sets.contains(combine(v1, i))) {
                            valid = false;
                        }
                    }
                    if (valid) {
                        set2.add(combine(v1, -1));
                    }
                }
            }
        }
        return set2;
    }

    private static int countOccurrences(List<String> v) {
        int count = 0;
        for (Set<String> s : datatable) {
            if (s.containsAll(v)) {
                count++;
            }
        }
        return count;
    }

    private static void subsets(List<String> items, List<String> v1, List<String> v2, int idx, PrintWriter fw) {
        if (idx == items.size()) {
            if (!v1.isEmpty() && !v2.isEmpty()) {
                int count1 = countOccurrences(items);
                int count2 = countOccurrences(v1);
                double conf = ((double) count1 / count2) * 100;
                if (conf >= confidence) {
                    fw.print("{ ");
                    v1.forEach(s -> fw.print(s + " "));
                    fw.print("}, ->, { ");
                    v2.forEach(s -> fw.print(s + " "));
                    fw.println("}, " + conf);
                }
            }
            return;
        }
        v1.add(items.get(idx));
        subsets(items, v1, v2, idx + 1, fw);
        v1.remove(v1.size() - 1);
        v2.add(items.get(idx));
        subsets(items, v1, v2, idx + 1, fw);
        v2.remove(v2.size() - 1);
    }

    private static void generateAssociationRules(Set<String> freqItems) throws IOException {
        try (PrintWriter fw = new PrintWriter(new FileWriter("freq_item_rules_output.csv"))) {
            for (String item : freqItems) {
                List<String> items = wordsOf(item);
                subsets(items, new ArrayList<>(), new ArrayList<>(), 0, fw);
            }
        }
    }

    public static void main(String[] args) {
        try {
            fin = new BufferedReader(new FileReader("freqitem.csv"));
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter Support % : ");
            minFrequency = scanner.nextDouble();

            System.out.print("Enter Confidence % : ");
            confidence = scanner.nextDouble();

            String line;
            while ((line = fin.readLine()) != null) {
                List<String> arr = wordsOf(line);
                Set<String> tmpset = new HashSet<>(arr);
                datatable.add(tmpset);

                for (String item : tmpset) {
                    products.add(item);
                    freq.put(item, freq.getOrDefault(item, 0) + 1);
                }
            }

            System.out.println("No of transactions: " + datatable.size());
            minFrequency = minFrequency * datatable.size() / 100;
            System.out.println("Min frequency: " + minFrequency);

            Queue<String> q = new LinkedList<>();
            for (String product : products) {
                if (freq.get(product) < minFrequency) {
                    q.add(product);
                }
            }
            while (!q.isEmpty()) {
                products.remove(q.poll());
            }

            int pass = 1;
            System.out.println("\nFrequent " + pass++ + " -item set : ");
            for (String product : products) {
                System.out.println("{" + product + "} " + freq.get(product));
            }

            int i = 2;
            Set<String> prev = cloneSet(products);
            while (i > 0) {
                Set<String> cur = aprioriGen(prev, i - 1);
                if (cur.isEmpty()) {
                    break;
                }

                for (String item : cur) {
                    List<String> arr = wordsOf(item);
                    int total = 0;
                    for (Set<String> s : datatable) {
                        if (s.containsAll(arr)) {
                            total++;
                        }
                    }
                    if (total >= minFrequency) {
                        freq.put(item, total);
                    } else {
                        q.add(item);
                    }
                }
                while (!q.isEmpty()) {
                    cur.remove(q.poll());
                }

                System.out.println("\n\nFrequent " + pass++ + " -item set : ");
                for (String item : cur) {
                    System.out.println("{" + item + "} " + freq.get(item));
                }
                prev = cloneSet(cur);
                i++;
            }

            generateAssociationRules(prev);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
