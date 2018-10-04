package cecs429.queryparser;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.Milestone1TokenProcessor;
import java.util.ArrayList;
import java.util.List;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {
//TODO::MICHAEL
    /**
     *
     */
    private String mTerm;

    /**
     *
     * @param term
     */
    public TermLiteral(String term) {
        mTerm = term;
    }

    /**
     *
     * @return
     */
    public String getTerm() {
        return mTerm;
    }

    @Override
    public List<Posting> getPostings(Index index) {
        Milestone1TokenProcessor processor = new Milestone1TokenProcessor();

        String[] stemmedStuff = processor.processTokens(mTerm);

        ArrayList<QueryComponent> words = new ArrayList<>();

        if (stemmedStuff.length == 1)
            return index.getPostings(stemmedStuff[0]);

        for (int i = 0; i < stemmedStuff.length; i++)
            words.add(new TermLiteral(stemmedStuff[i]));

        if (stemmedStuff.length == 1) {
            return index.getPostings(stemmedStuff[0]);
        }
        for (int i = 0; i < stemmedStuff.length; i++) {
            words.add(new TermLiteral(stemmedStuff[i]));
        }
        return (new OrQuery(words)).getPostings(index);
    }

    @Override
    public String toString() {
        return mTerm;
    }
}