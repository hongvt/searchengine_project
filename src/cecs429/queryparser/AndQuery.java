package cecs429.queryparser;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class AndQuery implements QueryComponent {
    /**
     * Contains the components to And together
     */
    private List<QueryComponent> mComponents;

    /**
     * Constructor used to create an AndQuery
     *
     * @param components List<QueryComponent> - mComponents is set to this parameter value (!!reference!!)
     */
    public AndQuery(List<QueryComponent> components) {
        mComponents = components;
    }

    /**
     * Uses the andMerge routine from lecture to merge two lists that share the same documentId.
     * Iterates through the lists with i and j pointers.
     *
     * @param list_one - the first list of the parameter
     * @param list_two - the second list of the parameter
     * @return the list of postings that are shared across both parameters.
     */
    private List<Posting> andMerge(List<Posting> list_one, List<Posting> list_two) {
        int i = 0, j = 0;
        List<Posting> result = new ArrayList<>();

        while (true) {
            if (list_one == null || list_two == null || i == list_one.size() || j == list_two.size())
                return result;
            else if (list_one.get(i).getDocumentId() == list_two.get(j).getDocumentId()) {
                result.add(new Posting(list_one.get(i).getDocumentId()));
                i++;
                j++;
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
        for (QueryComponent x : mComponents)
            postingList.add(x.getPostings(index));

        List<Posting> result = postingList.get(0);

        for (int i = 1; i < postingList.size(); i++) {
            result = andMerge(result, postingList.get(i));
        }
        return result;
    }

    @Override
    public String toString() {
        return
                String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
    }
}