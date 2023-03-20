import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class App {
    public static void main(String[] args) throws Exception {

        Path currentPath = Paths.get("Text Files");
        String folderPath = currentPath.toAbsolutePath().toString();
        File folder = new File(folderPath);
        File[] fileList = folder.listFiles();

        HashMap<String, DictEntry> index = new HashMap<>();

        for (int i = 0; i < fileList.length; i++) {
            DataInputStream df = new DataInputStream(new FileInputStream(fileList[i]));
            String line;
            while ((line = df.readLine()) != null) {
                // Input: "The quick brown fox jumps over the lazy dog."
                // Output: ["The", "quick", "brown", "fox", "jumps", "over", "the", "lazy",
                // "dog"]
                String[] words = line.split("\\W+");
                for (String word : words) {
                    // System.out.println(i+" "+word);
                    if (index.containsKey(word)) {
                        DictEntry entry = index.get(word);
                        Posting currPosting = entry.pList;
                        while (currPosting.docId != i && currPosting.next != null) {
                            currPosting = currPosting.next;
                        }
                        if (currPosting.docId == i) {
                            currPosting.dtf++;
                            entry.term_freq++;
                        } else {
                            Posting newPosting = new Posting(i);
                            currPosting.next = newPosting;
                            entry.doc_freq++;
                            entry.term_freq++;
                        }

                    } else {
                        DictEntry entry = new DictEntry();
                        entry.pList = new Posting(i);
                        index.put(word, entry);
                    }
                }

            }
            df.close();
        }
        HelperFunctions.PrintMenu(index);

    }
}