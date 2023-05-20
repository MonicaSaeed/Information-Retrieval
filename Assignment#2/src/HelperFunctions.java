import java.util.ArrayList;
import java.util.Collections;
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

    /*public static void PrintAll(HashMap<String, DictEntry> index) {
        index.forEach((key, entry) -> {
            System.out.println(key + " -> doc_freq: " + entry.doc_freq + " ,term_freq: " + entry.term_freq);
            Posting currPosting = entry.pList;
            while (currPosting != null) {
                System.out.println("docId: " + currPosting.docId + " ,dtf: " + currPosting.dtf);
                currPosting = currPosting.next;
            }
            System.out.println("");
        });
    }*/
    
    public static void PrintAll(HashMap<String, DictEntry> index) {
        ArrayList<String> sortedKeys = new ArrayList<>(index.keySet());
        Collections.sort(sortedKeys);
        for (String key : sortedKeys) {
            DictEntry entry = index.get(key);
            System.out.println(key + " -> doc_freq: " + entry.doc_freq + " ,term_freq: " + entry.term_freq);
            Posting currPosting = entry.pList;
            while (currPosting != null) {
                System.out.println(currPosting.toString());
                currPosting = currPosting.next;
            }
            System.out.println("");
        }
        index.forEach((key, entry) -> {
        });
    }


    //get a query ( set of a number of words) and return the list of documents that contain all the words in the query
    public static ArrayList<Integer> getQueryResult(HashMap<String, DictEntry> index, String[] query) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        ArrayList<ArrayList<Posting>> allPostings = new ArrayList<ArrayList<Posting>>();
        for (String word : query) {
            if (index.containsKey(word)) {
                DictEntry entry = index.get(word);
                Posting currPosting = entry.pList;
                ArrayList<Posting> currPostingList = new ArrayList<Posting>();
                while (currPosting != null) {
                    currPostingList.add(currPosting);
                    currPosting = currPosting.next;
                }
                allPostings.add(currPostingList);
            } else {
                System.out.println(word + "doesn't exist in any file");
                return result;
            }
        }
        
        ArrayList<Posting> resultDocIds = allPostings.get(0);
        for (int i = 1; i < allPostings.size(); i++) {
            resultDocIds = intersect(resultDocIds, allPostings.get(i));
        }
        for (Posting p : resultDocIds) {
            result.add(p.docId);
        }
        return result;
    }
    // 1 2 3 4(5,17,19) 5(19)
    // 4(6,13,20) 5(20) 6 
    ////4(6,20)5(20)
    // 1 4(7,14) 6
    ////4(7)
    // 1 3 4(8)

    //get two posting lists and return the intersection of them
    public static ArrayList<Posting> intersect(ArrayList<Posting> list1, ArrayList<Posting> list2) {
        ArrayList<Posting> result = new ArrayList<Posting>();
        int i = 0, j = 0;
        while (i < list1.size() && j < list2.size()) {
            if (list1.get(i).docId == list2.get(j).docId) {
                int pos1 = 0, pos2 = 0;
                ArrayList<Integer> positions1 = list1.get(i).positions;
                ArrayList<Integer> positions2 = list2.get(j).positions;
                ArrayList<Integer> positions = new ArrayList<Integer>();
                while (pos1 < positions1.size() && pos2 < positions2.size()) {
                    if (positions1.get(pos1) == positions2.get(pos2) - 1) {
                        positions.add(positions2.get(pos2));
                        pos1++;
                        pos2++;
                    } else if (positions1.get(pos1) < positions2.get(pos2) - 1) {
                        pos1++;
                    } else {
                        pos2++;
                    }
                }
                Posting p = list1.get(i);
                Posting newPosting = new Posting(p.docId, p.dtf, positions,p.next);
                result.add(newPosting);
                i++;
                j++;
            } else if (list1.get(i).docId < list2.get(j).docId) {
                i++;
            } else {
                j++;
            }
        }
        return result;
    }



    public static void PrintMenu(HashMap<String, DictEntry> index) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1.Print All\n2.Search\n3.Query\n4.Exit");
        System.out.print("Choose: ");
        int option = scanner.nextInt();
        if (option == 1) {
            HelperFunctions.PrintAll(index);
        } else if (option == 2) {
            System.out.print("Enter word to search for: ");
            String word = scanner.next();
            HelperFunctions.Search(index, word.toLowerCase());
        } else if (option == 3) {
            //String[] words = line.split("\\W+");
            Scanner scan = new Scanner(System.in);
            String word="";
            System.out.print("Enter query:");
            word += scan.nextLine();
            ArrayList<Integer> result = HelperFunctions.getQueryResult(index, word.split(" "));
            if(result.size() == 0){
                System.out.println("No results found");
            }
            else{
                System.out.println("Documents: ");
                for(int i = 0; i < result.size(); i++){
                    System.out.println(result.get(i));
                }
            }
            
        }
        else {
            scanner.close();
            return;
        }
        System.out.println("");
        PrintMenu(index);
        scanner.close();
    }
}