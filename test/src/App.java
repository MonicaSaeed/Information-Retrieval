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
                String[] words = line.split("\\W+");
                for (String word : words) {
                    ///Removing of Stop Words
                    if(word.equals("a") || word.equals("the") || word.equals("an") || word.equals("is") || word.equals("we") || word.equals("i") || word.equals("by") || word.equals("be") || word.equals("for") || word.equals("in")){
                        continue;
                    }else{
                        if (index.containsKey(word)) {
                            DictEntry entry = index.get(word);
                            Posting currPosting = entry.pList;
                            while (currPosting.docId != i+1 && currPosting.next != null) {
                                currPosting = currPosting.next;
                            }
                            if (currPosting.docId == i+1) {
                                currPosting.dtf++;
                                entry.term_freq++;
                            } else {
                                Posting newPosting = new Posting(i+1);
                                currPosting.next = newPosting;
                                entry.doc_freq++;
                                entry.term_freq++;
                            }
    
                        } else {
                            DictEntry entry = new DictEntry();
                            entry.pList = new Posting(i+1);
                            index.put(word, entry);
                        }
                    }
                    
                    
                }

            }
            df.close();
        }
        HelperFunctions.PrintMenu(index);

    }
}