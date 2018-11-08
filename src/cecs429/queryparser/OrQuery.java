package cecs429.queryparser;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent {
    /**
     * Contains the components to Or together
     */
    private List<QueryComponent> mComponents;

    /**
     * Constructor used to create an OrQuery
     *
     * @param components List<QueryComponent> - mComponents is set to this parameter value (!!reference!!)
     */
    public OrQuery(List<QueryComponent> components) {
        mComponents = components;
    }

    /**
     * Performs the orMerge by iterating through the smaller of the two lists
     * and checking to see if the the bigger list contains the posting(s) of smaller
     *
     * @param list_one - first part of the List of postings retrieved from mTerms
     * @param list_two - second part of the List of postings retrieved from mTerms
     * @return the two parameters lists merged together using the orMerge routine from lecture
     */
    private List<Posting> orMerge(List<Posting> list_one, List<Posting> list_two) {
        int i = 0, j = 0;
        List<Posting> result = new ArrayList<>();

        while (true) {
            if (i == list_one.size() || j == list_two.size())
                return result;
            else if (list_one.get(i).getDocumentId() == list_two.get(j).getDocumentId()) {
                result.add(new Posting(list_one.get(i).getDocumentId()));
                i++;
                j++;
            } else {
                if (list_one.get(i).getDocumentId() < list_two.get(j).getDocumentId()) {
                    result.add(new Posting(list_one.get(i).getDocumentId()));
                    i++;
                } else {
                    result.add(new Posting(list_two.get(j).getDocumentId()));
                    j++;
                }
            }
        }
    }

    @Override
    public List<Posting> getPostings(Index index) {
        List<List<Posting>> postingList = new ArrayList<>();
        for (QueryComponent x : mComponents)
            postingList.add(x.getPostings(index));

        List<Posting> result = postingList.get(0);

        for (int i = 1; i < postingList.size(); i++)
            result = orMerge(result, postingList.get(i));

        return result;
    }

    @Override
    public String toString() {
        // Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
        return "(" +
                String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
                + " )";
    }
}