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


        for (int i = 0; i < mComponents.size(); i++) {
            for (int j = 0; j < index.getVocabulary().size(); j++) {
                for (int k = 0; k < index.getPostings(index.getVocabulary().get(j)).size(); k++) {
                    if (index.getPostings(index.getVocabulary().get(j)).contains(mComponents.get(i).getPostings(index))) {
                        result.add(new Posting(index.getPostings(index.getVocabulary().get(j)).get(k).getDocumentId()));
                    }
                }
            }
        }
        // TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
        // intersecting the resulting postings.

        return result;
    }

    @Override
    public String toString() {
        return
                String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
    }
}
