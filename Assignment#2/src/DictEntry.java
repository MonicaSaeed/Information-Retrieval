public class DictEntry {
    int doc_freq = 1; // number of documents that contain the term
    int term_freq = 1; //number of times the term is mentioned in the collection
    Posting pList = null;
}
