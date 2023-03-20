import java.util.HashMap;
import java.util.Scanner;

public class HelperFunctions {
     public static void Search(HashMap<String, DictEntry> index, String word) {
        if (index.containsKey(word)) {
            DictEntry entry = index.get(word);
            System.out.println(word + " -> doc_freq: " + entry.doc_freq + " ,term_freq: " + entry.term_freq);
            Posting currPosting = entry.pList;
            System.out.println("All files: ");
            while (currPosting != null) {
                System.out.println("docId: "+currPosting.docId + " ,dtf: " + currPosting.dtf);
                currPosting = currPosting.next;
            }
            System.out.println("");
        } else {
            System.out.println("This word doesn't exist in any file");
            System.out.println("");
        }
    }

    public static void PrintAll(HashMap<String, DictEntry> index) {
        index.forEach((key, entry) -> {
            System.out.println(key + " -> doc_freq: " + entry.doc_freq + " ,term_freq: " + entry.term_freq);
            Posting currPosting = entry.pList;
            while (currPosting != null) {
                System.out.println("docId: " + currPosting.docId + " ,dtf: " + currPosting.dtf);
                currPosting = currPosting.next;
            }
            System.out.println("");
        });
    }

    public static void PrintMenu(HashMap<String, DictEntry> index) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1. Print All\n2.Search\n3. Exit");
        System.out.print("Choose: ");
        int option = scanner.nextInt();
        if (option == 1) {
            HelperFunctions.PrintAll(index);
        } else if (option == 2) {
            System.out.print("Enter word to search for: ");
            String word = scanner.next();
            HelperFunctions.Search(index, word);
        } else {
            scanner.close();
            return;
        }
        System.out.println("");
        PrintMenu(index);
        scanner.close();
    }
}