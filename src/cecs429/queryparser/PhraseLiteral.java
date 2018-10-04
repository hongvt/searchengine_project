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
    /**
     * The list of individual terms in the phrase.
     */
    private List<String> mTerms;

    /**
     * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
     * Instantiates the mTerms list and fills it
     * @param terms String - the String that was in double quotes that need to be represented as PhraseLiteral
     */
    public PhraseLiteral(String terms) {
        mTerms = new ArrayList<>();
        mTerms.addAll(Arrays.asList(terms.split(" ")));
    }

    /**
     * Performs the positional merge routine by iterating to find the same documentId across both lists
     * and checking to see if their position lists are off by 1
     * @param list_one - the first list of postings retrieved from mTerms
     * @param list_two - the second list of postings retrieved from mTerms
     * @return returns a result list of postings where two lists are contained within the same
     * documentId and where the position their positions are off by 1, meaning adjacent to each other
     */
    private List<Posting> positionMerge(List<Posting> list_one, List<Posting> list_two) {
        List<Posting> result = new ArrayList<>();
        int i = 0, j = 0, k, m;

        // first loop attempts to find the indexes where the document id's match
        while (true) {
            if (i == list_one.size() || j == list_two.size())
                return result;
            else if (list_one.get(i).getDocumentId() == list_two.get(j).getDocumentId()) {
                // when matched we attempt to find any positions that are off by 1 and add them accordingly
                k = 0;
                m = 0;
                while (true) {
                    if (k == list_one.get(i).getPositions().size() || m == list_two.get(j).getPositions().size()) {
                        i++;
                        j++;
                        break;
                    } else if (list_one.get(i).getPositions().get(k) + 1 == list_two.get(j).getPositions().get(m)) {
                        // check if empty first to avoid null pointer exception
                        if (result.isEmpty()) {
                            result.add(new Posting(list_two.get(j).getDocumentId()));
                            result.get(0).addPosition(list_two.get(j).getPositions().get(m));
                        } else if (result.get(result.size() - 1).getDocumentId() == list_two.get(j).getDocumentId()) {
                            result.get(result.size() - 1).addPosition(list_two.get(j).getPositions().get(m));
                        } else {
                            result.add(new Posting(list_two.get(j).getDocumentId()));
                            result.get(result.size() - 1).addPosition(list_two.get(j).getPositions().get(m));
                        }
                        k++;
                    } else {
                        if (list_one.get(i).getPositions().get(k) + 1 < list_two.get(j).getPositions().get(m))
                            k++;
                        else
                            m++;
                    }
                }
            } else {
                if (list_one.get(i).getDocumentId() < list_two.get(j).getDocumentId())
                    i++;
                else
                    j++;
            }
        }
    }

    @Override
    public List<Posting> getPostings(Index index) {
        List<List<Posting>> postingList = new ArrayList<>();

        for (int i = 0; i < mTerms.size(); i++) {
            String temp = index.getProcessor().getStem(index.getProcessor().removeNonAlphaNumCharBegEndAndQuotes(mTerms.get(i)));
            postingList.add(index.getPostings(temp));
        }

        List<Posting> result = postingList.get(0);

        for (int i = 1; i < postingList.size(); i++)
        {
            result = positionMerge(result, postingList.get(i));
        }


        return result;
        // TODO: program this method. Retrieve the postings for the individual terms in the phrase,
        // and positional merge them together.
    }

    @Override
    public String toString() {
        return "\"" + String.join(" ", mTerms) + "\"";
    }
}