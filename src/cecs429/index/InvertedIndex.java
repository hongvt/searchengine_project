package cecs429.index;

import cecs429.text.TokenProcessor;

import java.util.*;

public class InvertedIndex implements Index
{
    private final HashMap<String,ArrayList<Integer>> mIndex;
    private final List<String> mVocabulary;

    public InvertedIndex() {
        mIndex = new HashMap<>();
        mVocabulary = new ArrayList<>();
    }

    public void addTerm(String term, int documentId)
    {
        if(mIndex.containsKey(term))
        {
            if(mIndex.get(term).get(mIndex.get(term).size()-1) != documentId)
            {
                mIndex.get(term).add(documentId);
            }
            return;
        }
        ArrayList<Integer> docIds = new ArrayList<>();
        docIds.add(documentId);
        mIndex.put(term,docIds);
        mVocabulary.add(term);
        return;
    }

    @Override
    public List<Posting> getPostings(String term)
    {
        List<Posting> results = new ArrayList<>();
        if (mIndex.containsKey(term)) {
            for (int i = 0; i < mIndex.get(term).size(); i++) {
                results.add(new Posting(mIndex.get(term).get(i)));
            }
        }
        return results;
    }

    @Override
    public List<String> getVocabulary() {
        Collections.sort(mVocabulary);
        return Collections.unmodifiableList(mVocabulary);
    }

    @Override
    public void addToKGI(String[] types) {

    }

    @Override
    public String[] getWildcardMatches(String term) {
        return new String[0];
    }

    @Override
    public TokenProcessor getProcessor() {
        return null;
    }

}
