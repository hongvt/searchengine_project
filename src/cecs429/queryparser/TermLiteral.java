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
    private String mTerm;

    public TermLiteral(String term) {
        mTerm = term;
    }

    public String getTerm() {
        return mTerm;
    }

    @Override
    public List<Posting> getPostings(Index index) {
        Milestone1TokenProcessor processor = new Milestone1TokenProcessor();

        String[] stemmedStuff = processor.processTokens(mTerm);

        ArrayList<QueryComponent> words = new ArrayList<>();
<<<<<<< HEAD

        if (stemmedStuff.length == 1)
            return index.getPostings(stemmedStuff[0]);

        for (int i = 0; i < stemmedStuff.length; i++)
            words.add(new TermLiteral(stemmedStuff[i]));

=======
        if (stemmedStuff.length == 1)
        {
            return index.getPostings(stemmedStuff[0]);
        }
        for(int i = 0; i < stemmedStuff.length; i++)
        {
            words.add(new TermLiteral(stemmedStuff[i]));
        }
>>>>>>> 5556e44a38bb952076adfe65cbade427a449f1d7
        return (new OrQuery(words)).getPostings(index);
    }

    @Override
    public String toString() {
        return mTerm;
    }
}