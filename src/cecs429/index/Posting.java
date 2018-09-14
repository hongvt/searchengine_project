package cecs429.index;

import java.util.ArrayList;
import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {

    private int mDocumentId;
    private ArrayList<Integer> positions = new ArrayList<>();

    public Posting(int documentId) {
        mDocumentId = documentId;
    }

    public int getDocumentId() {
        return mDocumentId;
    }

    public List<Integer> getPositions() {
        return positions;
    }
}
