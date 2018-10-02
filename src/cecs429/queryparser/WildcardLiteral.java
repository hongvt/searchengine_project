package cecs429.queryparser;

import cecs429.index.Index;
import cecs429.index.Posting;
import java.util.ArrayList;
import java.util.List;

public class WildcardLiteral implements QueryComponent {

    private String mTerm;

    public WildcardLiteral(String term) {
        mTerm = term;
    }

    @Override
    public List<Posting> getPostings(Index index) {
        String[] matches = index.getWildcardMatches(mTerm);
        String[] typesStems = index.getProcessor().getStems(matches);
        ArrayList<Posting> orPosts = new ArrayList<>();

        for (int i = 0; i < typesStems.length; i++) {
            for (int j = 0; j < index.getPostings(typesStems[i]).size(); j++) {
                if (orPosts.isEmpty() || !orPosts.contains(index.getPostings(typesStems[i]).get(j))) {
                    orPosts.add(index.getPostings(typesStems[i]).get(j));
                }
            }
        }
        return orPosts;
    }
}