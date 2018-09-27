package cecs429.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        return new ArrayList<>(positions);
    }

    public void addPosition(Integer pos)
    {
        positions.add(pos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Posting posting = (Posting) o;
        if (this.positions.size() == posting.positions.size())
        {
            for (int i = 0; i < this.positions.size(); i++)
            {
                if (positions.get(i) != posting.positions.get(i))
                {
                    return false;
                }
            }
        }
        return mDocumentId == posting.mDocumentId;
    }

    @Override
    public String toString()
    {
        return "doc ID: " + getDocumentId() + "->" + positions.toString();
    }
}
