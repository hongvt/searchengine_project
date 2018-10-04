package cecs429.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting implements Comparable {
    /**
     * The doc ID where the search query component was found
     */
    private int documentId;
    /**
     * The positions the search query component is found within the documentId
     */
    private ArrayList<Integer> positions;

    /**
     * Constructor used to create a Posting object with a given document ID
     * Instantiates the positions ArrayList
     *
     * @param documentId int - the documentID is set to this parameter value
     */
    public Posting(int documentId) {
        this.documentId = documentId;
        this.positions = new ArrayList<>();
    }

    /**
     * @return int - documentId of the Posting
     */
    public int getDocumentId() {
        return this.documentId;
    }

    /**
     * @return List<Integer> - deep copy of the positions list (!!not the reference!!)
     */
    public List<Integer> getPositions() {
        return new ArrayList<>(positions);
    }

    /**
     * @param pos Integer - adds the parameter value to the positions list
     */
    public void addPosition(Integer pos) {
        positions.add(pos);
    }

    /**
     * A Posting is equal to another Posting if the documentId is the same
     * AKA the positions of a posting do not determine equality
     *
     * @param o Object
     * @return boolean - true if this is equal to the parameter value, else false
     */
    @Override
    public boolean equals(Object o) {
        Posting posting = (Posting) o;
        return this.documentId == posting.documentId;
    }

    /**
     * A Posting is compared to the parameter object based on their documentId
     *
     * @param o Object
     * @return int - 1 if this is greater than parameter value, -1 if this is less than parameter value, 0 if this is equal to parameter value
     */
    @Override
    public int compareTo(Object o) {
        Posting posting = (Posting) o;
        return ((Integer) (this.documentId)).compareTo((Integer) (posting.documentId));
    }

    /**
     * @return String - document ID and the positions of the search query component
     */
    @Override
    public String toString() {
        return "doc ID: " + getDocumentId() + "->" + positions.toString();
    }
}