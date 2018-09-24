package cecs429.queryparser;

import cecs429.index.Index;
import cecs429.index.Posting;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent {
    // The components of the Or query.
    private List<QueryComponent> mComponents;

    public OrQuery(List<QueryComponent> components) {
        mComponents = components;
    }

    private List<Posting> orMerge(List<Posting> list_one, List<Posting> list_two){
        List<Posting> result = new ArrayList<>();
        int i = 0, j = 0;

        while (true) {
            if (i == list_one.size() || j == list_two.size())
                return result;
            else if (list_one.get(i).getDocumentId() == list_two.get(j).getDocumentId()) {
                result.add(list_one.get(i));
                i++;
                j++;
            } else {
                if (list_one.get(i).getDocumentId() < list_two.get(j).getDocumentId()) {
                    result.add(list_one.get(i));
                    i++;
                } else {
                    result.add(list_two.get(j));
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
