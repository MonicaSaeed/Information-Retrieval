public class Posting {
    Posting(int docId) {
        this.docId = docId;
    }
    public Posting next = null;
    int docId;
    int dtf = 1; // document term frequency
}
