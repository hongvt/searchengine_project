package cecs429.queryparser;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements QueryComponent {
    // The list of individual terms in the phrase.
    private List<String> mTerms = new ArrayList<>();

    /**
     * Constructs a PhraseLiteral with the given individual phrase terms.
     */
    public PhraseLiteral(List<String> terms) {
        mTerms.addAll(terms);
    }

    /**
     * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
     */
    public PhraseLiteral(String terms) {
        mTerms.addAll(Arrays.asList(terms.split(" ")));
    }

    @Override
    public List<Posting> getPostings(Index index) {

        List<Posting> list_one = index.getPostings(mTerms.get(0));
        List<Posting> list_two = index.getPostings(mTerms.get(1));
        List<Posting> result = new ArrayList<>();
        int i = 0, j = 0, k;

        while (true) {
            if (i == list_one.size() || j == list_two.size()) {
                return result;
            } else if (list_one.get(i).getDocumentId() == list_two.get(j).getDocumentId()) {
                k = 0;
                while (true) {
                    if (k == list_one.get(0).getPositions().size() || k == list_two.get(1).getPositions().size()) {
                        break;
                    } else if (list_one.get(i).getPositions().get(k) + 1 == list_two.get(j).getPositions().get(k)) {
                        result.add(new Posting(list_one.get(i).getDocumentId()));
                        result.get(result.size() - 1).getPositions().add(list_one.get(i).getPositions().get(k));
                    } else
                        k++;
                }
            } else {
                if (list_one.get(i).getDocumentId() < list_two.get(j).getDocumentId())
                    i++;
                else
                    j++;
            }
        }
        // TODO: program this method. Retrieve the postings for the individual terms in the phrase,
        // and positional merge them together.
    }

    @Override
    public String toString() {
        return "\"" + String.join(" ", mTerms) + "\"";
    }
}
