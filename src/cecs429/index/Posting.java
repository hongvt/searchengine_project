package cecs429.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {

    /**
     *
     */
    private int documentId;
    /**
     *
     */
    private ArrayList<Integer> positions = new ArrayList<>();

    /**
     *
     * @param documentId
     */
    public Posting(int documentId) {
        this.documentId = documentId;
    }

    /**
     *
     * @return
     */
    public int getDocumentId() {
        return this.documentId;
    }

    /**
     *
     * @return
     */
    public List<Integer> getPositions() {
        return new ArrayList<>(positions);
    }

    /**
     *
     * @param pos
     */
    public void addPosition(Integer pos)
    {
        positions.add(pos);
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        Posting posting = (Posting) o;
        return this.documentId == posting.documentId;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString()
    {
        return "doc ID: " + getDocumentId() + "->" + positions.toString();
    }
}