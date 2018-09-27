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
<<<<<<< HEAD
=======
    // pass in tokenprocessor to getpostings
>>>>>>> 5556e44a38bb952076adfe65cbade427a449f1d7
    public List<Posting> getPostings(Index index) {
        return (new KGramIndex(mTerm)).getPostings(index);
    }
}