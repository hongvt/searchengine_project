package cecs429.queryparser;

import cecs429.index.Index;
import cecs429.index.Posting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * A WildcardLiteral represents a single term with at least one asterisks in the subquery
 */
public class WildcardLiteral implements QueryComponent {
    /**
     * The term with at least one asterisks
     */
    private String mTerm;

    /**
     * Constructor that creates the WildcardLiteral given the term
     * @param term String - the wildcard; mTerm is set to this parameter value
     */
    public WildcardLiteral(String term) {
        mTerm = term;
    }

    @Override
    public List<Posting> getPostings(Index index) {
        String[] matches = index.getWildcardMatches(mTerm);
        String[] typesStems = index.getProcessor().getStems(matches);
        HashSet<Posting> orPosts = new HashSet<>();

        for (int i = 0; i < typesStems.length; i++) {
            for (int j = 0; j < index.getPostingsWithPositions(typesStems[i]).size(); j++) {
                orPosts.add(index.getPostingsWithPositions(typesStems[i]).get(j));
            }
        }

        ArrayList<Posting> orPostsList = new ArrayList<>(orPosts);
        Collections.sort(orPostsList);
        return orPostsList;
    }
}