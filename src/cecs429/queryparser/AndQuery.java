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
    private List<QueryComponent> mComponents;

    public AndQuery(List<QueryComponent> components) {
        mComponents = components;
    }

    private List<Posting> andMerge(List<Posting> list_one, List<Posting> list_two){
        int i = 0, j = 0;
        List<Posting> result = new ArrayList<>();

        while (true) {
            if (i == list_one.size() || j == list_two.size())
                return result;
            else if (list_one.get(i).getDocumentId() == list_two.get(j).getDocumentId()) {
                result.add(list_one.get(i));
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

        for (int i = 1; i < postingList.size(); i++)
            result = andMerge(result, postingList.get(i));

        return result;
    }

    @Override
    public String toString() {
        return
                String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
    }
}