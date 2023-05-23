import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class APPWITHHINTS {
    public static void main(String[] args) throws Exception {
        // Read 10 text files
        Path currentPath = Paths.get("Text Files");
        String folderPath = currentPath.toAbsolutePath().toString();
        File folder = new File(folderPath);
        File[] fileList = folder.listFiles();

        HashMap<String, DictEntry> index = buildInvertedIndex(fileList);

        Scanner scanner = new Scanner(System.in);

        //scanner.nextLine(); // Consume newline character
        System.out.print("Enter a query (separated by space): ");
        String queryInput = scanner.nextLine();
        List<String> query = Arrays.asList(queryInput.split(" "));
        computeCosineSimilarity(index, fileList, query);
        //scanner.close();
    }
    //  Build the inverted index
    private static HashMap<String, DictEntry> buildInvertedIndex(File[] fileList) throws IOException {
        HashMap<String, DictEntry> index = new HashMap<>();

        for (int i = 0; i < fileList.length; i++) {
            try (DataInputStream df = new DataInputStream(new FileInputStream(fileList[i]))) {
                String line;
                while ((line = df.readLine()) != null) {
                    String[] words = line.split("\\W+");
                    for (String word : words) {
                        if (isStopWord(word)) {
                            continue;
                        } else {
                            if (index.containsKey(word)) {
                                DictEntry entry = index.get(word);
                                Posting currPosting = entry.pList;
                                while (currPosting.docId != i + 1 && currPosting.next != null) {
                                    currPosting = currPosting.next;
                                }
                                if (currPosting.docId == i + 1) {
                                    currPosting.dtf++;
                                    entry.term_freq++;
                                } else {
                                    Posting newPosting = new Posting(i + 1);
                                    currPosting.next = newPosting;
                                    entry.doc_freq++;
                                    entry.term_freq++;
                                }
                            } else {
                                DictEntry entry = new DictEntry();
                                entry.pList = new Posting(i + 1);
                                index.put(word, entry);
                            }
                        }
                    }
                }
            }
        }

        return index;
    }

    private static boolean isStopWord(String word) {
        List<String> stopWords = Arrays.asList("a", "the", "an", "is", "we", "i", "by", "be", "for", "in");
        return stopWords.contains(word);
    }


    private static void computeCosineSimilarity(HashMap<String, DictEntry> index, File[] fileList, List<String> query) {
        HashMap<Integer, Double> cosineSimilarities = new HashMap<>();
        HashMap<String, Double> queryVector = computeTermFrequencies(query);

        double queryMagnitude = calculateVectorMagnitude(queryVector);

        for (int i = 0; i < fileList.length; i++) {
            double dotProduct = 0.0;
            double docMagnitude = 0.0;
            double tfidf = 0.0;

            try (DataInputStream df = new DataInputStream(new FileInputStream(fileList[i]))) {
                String line;
                while ((line = df.readLine()) != null) {
                    String[] words = line.split("\\W+");
                    for (String word : words) {
                        if (isStopWord(word)) {
                            continue;
                        } else {
                            if (index.containsKey(word)) {
                                DictEntry entry = index.get(word);
                                Posting currPosting = entry.pList;
                                while (currPosting != null) {
                                    if (currPosting.docId == i + 1) {
                                        tfidf = computeTFIDF(currPosting.dtf, entry.term_freq, entry.doc_freq, fileList.length);
                                        dotProduct += tfidf * queryVector.getOrDefault(word, 0.0);
                                        break;
                                    }
                                    currPosting = currPosting.next;
                                }
                                docMagnitude += Math.pow(tfidf, 2);
                            }
                        }
                    }
                }

                // Calculate the magnitude of the document vector
                docMagnitude = Math.sqrt(docMagnitude);

                // Calculate the cosine similarity
                double cosineSimilarity = dotProduct / (docMagnitude * queryMagnitude);

                // Store the cosine similarity for the current document
                cosineSimilarities.put(i + 1, cosineSimilarity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        rankFiles(cosineSimilarities, fileList);
    }

    private static double computeTFIDF(int termFrequency, int totalTermFrequency, int documentFrequency, int totalDocuments) {
        double tf = termFrequency > 0 ? 1 + Math.log(termFrequency) : 0.0;
        double idf = Math.log((double) totalDocuments / documentFrequency);
        return tf * idf;
    }

    private static double calculateVectorMagnitude(HashMap<String, Double> vector) {
        double magnitude = 0.0;
        for (double value : vector.values()) {
            magnitude += Math.pow(value, 2);
        }
        return Math.sqrt(magnitude);
    }




    private static HashMap<String, Double> computeTermFrequencies(List<String> words) {
        HashMap<String, Double> termFrequencies = new HashMap<>();
        double queryMagnitude = 0.0;

        for (String word : words) {
            termFrequencies.put(word, termFrequencies.getOrDefault(word, 0.0) + 1.0);
            queryMagnitude += Math.pow(termFrequencies.get(word), 2);
        }

        // Calculate the magnitude of the query vector
        queryMagnitude = Math.sqrt(queryMagnitude);

        // Normalize the query vector
        for (String word : termFrequencies.keySet()) {
            termFrequencies.put(word, termFrequencies.get(word) / queryMagnitude);
        }

        return termFrequencies;
    }



    private static void rankFiles(HashMap<Integer, Double> cosineSimilarities, File[] fileList) {
        List<Map.Entry<Integer, Double>> rankedFiles = new ArrayList<>(cosineSimilarities.entrySet());
        rankedFiles.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        System.out.println("Ranked Files:");
        for (Map.Entry<Integer, Double> entry : rankedFiles) {
            int docId = entry.getKey();
            double similarity = entry.getValue();
            if (docId > 0 && docId <= fileList.length) {
                File file = fileList[docId - 1];
                System.out.println("File: " + file.getName() + ", Cosine Similarity: " + similarity);
            }
        }
    }



}
