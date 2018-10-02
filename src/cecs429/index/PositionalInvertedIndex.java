package cecs429.index;

import cecs429.queryparser.KGramIndex;
import cecs429.text.Milestone1TokenProcessor;
import cecs429.text.TokenProcessor;

import java.util.*;

public class PositionalInvertedIndex implements Index {

    private HashMap<String, ArrayList<Posting>> index;
    private HashSet<String> vocabTypes;
    private KGramIndex kgi;
    private TokenProcessor processor;

    public PositionalInvertedIndex(TokenProcessor processor) {
        index = new HashMap<>();
        vocabTypes = new HashSet<>();
        kgi = new KGramIndex();
        this.processor = processor;
    }

    @Override
    public void addToKGI(String token)
    {
        kgi.addToKGI(processor.processButDontStemTokensAKAGetType(token));
    }

    @Override
    public String[] getWildcardMatches(String term)
    {
        return kgi.getWildcardMatches(term);
    }

    @Override
    public TokenProcessor getProcessor()
    {
        return this.processor;
    }

    @Override
    public List<Posting> getPostings(String term) {
        List<Posting> results = new ArrayList<>();

        try {
            for (Posting x : index.get(term)) {
                results.add(x);
            }
        } catch (NullPointerException e) {
            System.out.println(term + " was not found");
        }

        return results;
    }

    @Override
    public List<String> getVocabulary() {
        Collection<String> unsortedVocab = index.keySet();
        ArrayList<String> sortedVocab = new ArrayList(unsortedVocab);
        Collections.sort(sortedVocab);
        return Collections.unmodifiableList(sortedVocab);
    }

    public void addTerm(String term, int documentId, int position) {
        if (index.containsKey(term)) {
            if (index.get(term).get(index.get(term).size() - 1).getDocumentId() != documentId) {
                index.get(term).add(new Posting(documentId));
            }
        } else {
            index.put(term, new ArrayList<>());
            index.get(term).add(new Posting(documentId));
        }

        // find the index at which the documentId parameter starts at
        for (int docIdIndex = 0; docIdIndex < index.get(term).size(); docIdIndex++) {
            if (index.get(term).get(docIdIndex).getDocumentId() == documentId) {
                index.get(term).get(docIdIndex).addPosition(position);
            }
        }
    }
}