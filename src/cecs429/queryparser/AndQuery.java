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

    @Override
    public List<Posting> getPostings(Index index) {
        List<Posting> result = new ArrayList<>();
        List<Posting> list_one = mComponents.get(0).getPostings(index);
        List<Posting> list_two = mComponents.get(1).getPostings(index);
        int i = 0, j = 0;

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
    public String toString() {
        return
                String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
    }
}