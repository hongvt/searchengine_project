package cecs429.queryparser;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.List;

public class WildcardLiteral implements QueryComponent {

    private String mTerm;

    public WildcardLiteral(String term) {
        mTerm = term;
    }

    @Override
    // pass in tokenprocessor to getpostings
    public List<Posting> getPostings(Index index) {
        return (new KGramIndex(mTerm)).getPostings(index);
    }
}
