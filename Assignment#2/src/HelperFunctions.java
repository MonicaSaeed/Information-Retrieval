import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;


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
    //(tota (nervana) monica) 
    // 1 2 3 4(5,17,19) 5(19) ->r
    // 4(6,13,20) 5(20) 6  ->r
    ////4(6,20)5(20) tota nervana ->doc 4(6,20), 5(20)
    // 1 4(7,14) 6 ->r
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

    public static ArrayList<Float> getCosineSimilarity(HashMap<String, DictEntry> index, String[] query, int N) {
        Map<String, Integer> queryTermFrequency = computeTermFrequencies(query);
        ArrayList<Float> result = new ArrayList<Float>();
        ArrayList<ArrayList<Integer>> cosineVector = new ArrayList<>();
        // row 0 is for query
        cosineVector.add(new ArrayList<Integer>());
        for (Map.Entry<String, Integer> mapEntry : queryTermFrequency.entrySet()) {
            cosineVector.get(0).add(mapEntry.getValue());
        }
        // row 1 to N is for documents
        for (int i = 1; i <= N; i++) {
            cosineVector.add(new ArrayList<Integer>());
            for (int j = 0; j < queryTermFrequency.size(); j++) {
                cosineVector.get(i).add(0);
            }
        }
        //// a b c d a a b d
        // 3 2 1 2
        // 0 0 0 0
        //.....(N)
        // 0 0 0 0
        int i = 0;
        for (Map.Entry<String, Integer> mapEntry : queryTermFrequency.entrySet()) {
            String word = mapEntry.getKey();
            if (index.containsKey(word)) {
                DictEntry entry = index.get(word);
                Posting currPosting = entry.pList;
                while (currPosting != null) {
                    cosineVector.get(currPosting.docId).set(i, currPosting.dtf);
                    currPosting = currPosting.next;
                }
            }
            i++;
        }

        result = computeCosineSimilarity(cosineVector);
        
        return result;
    }

    
    private static ArrayList<Float> computeCosineSimilarity(ArrayList<ArrayList<Integer>> cosineVector) {
        ArrayList<Float> result = new ArrayList<Float>();
        for (int i = 1; i < cosineVector.size(); i++) {
            float cosineSimilarity = 0;
            float dotProduct = 0;
            float queryMagnitude = 0;
            float docMagnitude = 0;
            for (int j = 0; j < cosineVector.get(0).size(); j++) {
                dotProduct += cosineVector.get(0).get(j) * cosineVector.get(i).get(j);
                queryMagnitude += cosineVector.get(0).get(j) * cosineVector.get(0).get(j);
                docMagnitude += cosineVector.get(i).get(j) * cosineVector.get(i).get(j);
            }
            cosineSimilarity = (float) (dotProduct / (Math.sqrt(queryMagnitude) * Math.sqrt(docMagnitude)));
            result.add(cosineSimilarity);
        }
        return result;
    }

    //calculate the tf-idf of the term in the document
    public static float getTfIdf(HashMap<String, DictEntry> index, String term, int docId, int N) {
        return (getIdf(index, term, N) * (float)getTF(index, term, docId));
    }


    //calculate the idf of the term in the document
    public static float getIdf(HashMap<String, DictEntry> index, String term, int N) {
        float idf = 0;
        if (index.containsKey(term)) {
            DictEntry entry = index.get(term);  
            idf = (float) Math.log10(N / entry.doc_freq);
        }
        return idf;
    }
    //get the term frequency of the query
    public static int getTF(HashMap<String, DictEntry> index, String term, int docId){
        int tf = 0;
        if (index.containsKey(term)) {
            DictEntry entry = index.get(term);
            Posting currPosting = entry.pList;
            while (currPosting != null) {
                if (currPosting.docId == docId) {
                    tf = currPosting.dtf;
                    break;
                }
                currPosting = currPosting.next;
            }
        }
        return tf;
    }
    static Vector<String> links = new Vector<>();
    public static void getPageLinks(String URL) throws IOException {

        // 4. Check if you have already crawled the URLs
        // (we are intentionally not checking for duplicate content in this example)

       if (!links.contains(URL)) {
           try{
                // 4. (i) If not add it to the index
                if (links.add(URL)) {
                    System.out.println(URL);
                }
                // 2. Fetch the HTML code
                Document document = Jsoup.connect(URL).get();// jsoup jar to extract web data
                // 3. Parse the HTML to extract links to other URLs
                Elements linksOnPage = document.select("a[href]");

                // 5. For each extracted URL... go back to Step 4.
                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"));

                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }


    public static void PrintMenu(HashMap<String, DictEntry> index) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println(
                "1.Print All\n2.Search\n3.Query\n4.compute the cosine similarity between each file and the query\n5.web crawling\n6.calculate TfIdf\n7.calculate IDF\n8.Exit");
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
            String word = "";
            System.out.print("Enter query:");
            word += scan.nextLine();
            ArrayList<Integer> result = HelperFunctions.getQueryResult(index, word.split(" "));
            if (result.size() == 0) {
                System.out.println("No results found");
            } else {
                System.out.println("Documents: ");
                for (int i = 0; i < result.size(); i++) {
                    System.out.println(result.get(i));
                }
            }

        } else if (option == 4) {
            Scanner scan = new Scanner(System.in);
            String word = "";
            System.out.print("Enter query:");
            word += scan.nextLine();
            ArrayList<Float> result = HelperFunctions.getCosineSimilarity(index, word.split(" "), 10);
            System.out.println("Cosine similarity: ");
            for (int i = 0; i < result.size(); i++) {
                System.out.println(result.get(i));
            }
            System.out.println("rank the 10 files according to the value of the cosin similarity");
            //save id,cosine similarity to a hashmap
            HashMap<Integer, Float> map = new HashMap<>();
            for (int i = 0; i < result.size(); i++) {
                if(Double.isNaN(result.get(i))){
                    map.put(i+1, (float)0);
                }else{
                    map.put(i+1, result.get(i));
                }
                
            }
            //sort the hashmap
            List<Map.Entry<Integer, Float>> list = new ArrayList<Map.Entry<Integer, Float>>(map.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<Integer, Float>>() {
                //descending order
                public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            //print the sorted hashmap
            for (Map.Entry<Integer, Float> mapping : list) {
                System.out.println(mapping.getKey() + ":" + mapping.getValue());
            }

        }else if(option==5){
            Scanner scan = new Scanner(System.in);
            String link = "";
            System.out.print("Enter a link:");
            link += scan.nextLine();
            HelperFunctions.getPageLinks(link);
            System.out.println();

        } else if (option == 6) {
            Scanner scn = new Scanner(System.in);
            System.out.println("Enter a word:");
            String word = scn.nextLine();
            System.out.println("Enter a docId:");
            int docId = scn.nextInt();
            System.out.println("TfIdf: " + HelperFunctions.getTfIdf(index, word, docId, 10));
        } 
        else if(option==7){
            Scanner scn = new Scanner(System.in);
            System.out.println("Enter a word:");
            String word = scn.nextLine();
            System.out.println("Idf: " + HelperFunctions.getIdf(index, word, 10));
        }else {
            scanner.close();
            return;
        }
        System.out.println("");
        PrintMenu(index);
        scanner.close();
    }

    private static HashMap<String, Integer> computeTermFrequencies(String[] words) {
        HashMap<String, Integer> termFrequencies = new HashMap<>();

        for (String word : words) {
            termFrequencies.put(word, termFrequencies.getOrDefault(word, 0) + 1);
        }
        
        return termFrequencies;
    }



}