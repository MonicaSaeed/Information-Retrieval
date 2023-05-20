import java.util.ArrayList;

public class Posting {
    Posting(int docId) {
        this.docId = docId;
    }
    Posting(int docId,int dtf, ArrayList<Integer> position, Posting next) {
        this.docId = docId;
        this.dtf = dtf;
        this.positions = position;
        this.next = next;
    }
    public Posting next = null;
    int docId;
    int dtf = 1; // document term frequency
    ArrayList<Integer> positions = new ArrayList<Integer>();
    public void addPosition(int position) {
        positions.add(position);
    }
    public String toString() {
        String s = "docId: " + docId + ", dtf: " + dtf + ", positions: ";
        for (int i = 0; i < positions.size(); i++) {
            s += positions.get(i) + ",";
        }
        return s;
    }
}
