
import java.io.*;
import java.util.*;

public class FrequentItemset {
    static BufferedReader fin;
    static double minfre;
    static List<Set<String>> datatable = new ArrayList<>();
    static Set<String> products = new HashSet<>();
    static Map<String, Integer> freq = new HashMap<>();

    static List<String> wordsof(String str) {
        List<String> tmpset = new ArrayList<>();
        StringBuilder tmp = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            if (Character.isLetterOrDigit(str.charAt(i))) {
                tmp.append(str.charAt(i));
            } else {
                if (tmp.length() > 0) {
                    tmpset.add(tmp.toString());
                    tmp.setLength(0); 
                }
            }
            i++;
        }
        if (tmp.length() > 0) {
            tmpset.add(tmp.toString());
        }
        return tmpset;
    }

    static String combine(List<String> arr, int miss) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < arr.size(); i++) {
            if (i != miss) {
                str.append(arr.get(i)).append(" ");
            }
        }
        if (str.length() > 0) {
            str.setLength(str.length() - 1); 
        }
        return str.toString();
    }

    static Set<String> cloneit(Set<String> arr) {
        return new HashSet<>(arr); 
    }

    static Set<String> apriori_gen(Set<String> sets, int k) {
        Set<String> set2 = new HashSet<>();
        List<String> setList = new ArrayList<>(sets);
        
        for (int i = 0; i < setList.size(); i++) {
            for (int j = i + 1; j < setList.size(); j++) {
                List<String> v1 = wordsof(setList.get(i));
                List<String> v2 = wordsof(setList.get(j));

                boolean alleq = true;
                for (int m = 0; m < k - 1 && alleq; m++) {
                    if (!v1.get(m).equals(v2.get(m))) {
                        alleq = false;
                    }
                }

                if (alleq) {
                    v1.add(v2.get(k - 1));
                    if (v1.get(v1.size() - 1).compareTo(v1.get(v1.size() - 2)) < 0) {
                        String temp = v1.get(v1.size() - 1);
                        v1.set(v1.size() - 1, v1.get(v1.size() - 2));
                        v1.set(v1.size() - 2, temp);
                    }

                    boolean valid = true;
                    for (int m = 0; m < v1.size() && valid; m++) {
                        String tmp = combine(v1, m);
                        if (!sets.contains(tmp)) {
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

    public static void main(String[] args) {
        try {
            fin = new BufferedReader(new FileReader("freqitem.csv"));
        } catch (FileNotFoundException e) {
            System.err.println("Error in opening file: " + e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Frequency %: ");
        minfre = scanner.nextDouble();

        String str;
        try {
            while ((str = fin.readLine()) != null) {
                List<String> arr = wordsof(str);
                Set<String> tmpset = new HashSet<>(arr);
                datatable.add(tmpset);

                for (String item : tmpset) {
                    products.add(item);
                    freq.put(item, freq.getOrDefault(item, 0) + 1);
                }
            }
            fin.close();
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        System.out.println("No of transactions: " + datatable.size());
        minfre = minfre * datatable.size() / 100;
        System.out.println("Min frequency: " + minfre);

        Queue<String> q = new LinkedList<>();
        for (String item : products) {
            if (freq.get(item) < minfre) {
                q.add(item);
            }
        }
        while (!q.isEmpty()) {
            products.remove(q.poll());
        }

        int pass = 1;
        System.out.println("\nFrequent " + pass++ + "-item set: ");
        for (String it : products) {
            System.out.println("{" + it + "} " + freq.get(it));
        }

        int i = 2;
        Set<String> prev = cloneit(products);
        while (true) {
            Set<String> cur = apriori_gen(prev, i - 1);
            if (cur.isEmpty()) {
                break;
            }

            Iterator<String> it = cur.iterator();
            while (it.hasNext()) {
                String item = it.next();
                List<String> arr = wordsof(item);
                int tot = 0;

                for (Set<String> transaction : datatable) {
                    boolean pres = true;
                    for (String elem : arr) {
                        if (!transaction.contains(elem)) {
                            pres = false;
                            break;
                        }
                    }
                    if (pres) {
                        tot++;
                    }
                }

                if (tot >= minfre) {
                    freq.put(item, freq.getOrDefault(item, 0) + tot);
                } else {
                    it.remove(); 
                }
            }

            if (cur.isEmpty()) {
                break;
            }

            System.out.println("\nFrequent " + pass++ + "-item set: ");
            for (String itCur : cur) {
                System.out.println("{" + itCur + "} " + freq.get(itCur));
            }

            prev = cloneit(cur);
            i++;
        }

        try {
            BufferedWriter fw = new BufferedWriter(new FileWriter("Aprior_dataset_output.csv"));
            for (String it : prev) {
                fw.write("{" + it + "}");
                fw.newLine();
            }
            fw.close();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
