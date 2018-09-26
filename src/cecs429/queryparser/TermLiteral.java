package cecs429.queryparser;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.Milestone1TokenProcessor;
import cecs429.text.TokenProcessor;

import java.util.List;
import libstemmer_java.java.org.tartarus.snowball.SnowballStemmer;
import libstemmer_java.java.org.tartarus.snowball.ext.englishStemmer;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {
    private String mTerm;
    private SnowballStemmer snowballStemmer = new englishStemmer();

    public TermLiteral(String term) {
        mTerm = term;
    }

    public String getTerm() {
        return mTerm;
    }

    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        snowballStemmer.setCurrent(processor.processToken(mTerm));

        if(snowballStemmer.stem())
            return index.getPostings(snowballStemmer.getCurrent());

        return null;
    }

    @Override
    public String toString() {
        return mTerm;
    }
}
