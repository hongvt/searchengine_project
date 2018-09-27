package cecs429.queryparser;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;
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
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        List<List<Posting>> postingList = new ArrayList<>();
        for (QueryComponent x : mComponents)
            postingList.add(x.getPostings(index, processor));

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