package cecs429.queryparser;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.List;

public class WildcardLiteral implements QueryComponent {

    private String mTerm;

    public WildcardLiteral(String term) {
        mTerm = term;
    }

    @Override
    public List<Posting> getPostings(Index index) {
        return (new KGramIndex(mTerm)).getPostings(index);
    }
}