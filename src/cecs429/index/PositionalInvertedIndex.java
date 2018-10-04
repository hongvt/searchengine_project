package cecs429.index;

import cecs429.queryparser.KGramIndex;
import cecs429.text.TokenProcessor;

import java.util.*;

/**
 * Positional inverted index class which keeps track of the position
 * of a term in addition to which document it was found in
 */
public class PositionalInvertedIndex implements Index {
    /**
     * Key is the stemmed vocab
     * Value is the posting list (a posting is a doc ID and its positions in that doc)
     */
    private HashMap<String, ArrayList<Posting>> index;
    /**
     * The KGramIndex that is filled with this kgrams from the types of this Index
     */
    private KGramIndex kgi;
    /**
     * The TokenProcessor used to index the Index and for the queries on this Index
     */
    private TokenProcessor processor;

    /**
     * Constructor used to create a PositionalInvertedIndex with the given TokenProcessor parameter value
     * Instantiates the index HashMap and kgi KGramIndex
     *
     * @param processor TokenProcessor - processor is set to this parameter value
     */
    public PositionalInvertedIndex(TokenProcessor processor) {
        index = new HashMap<>();
        kgi = new KGramIndex();
        this.processor = processor;
    }

    /**
     * Checks to see if the term being added is within the index already, if it is, add a new Posting
     * for that term, else add the term to the index
     *
     * @param term       - the stemmed term to be added to the index
     * @param documentId - the documentId at which the corresponding term shows up in
     * @param position   - the position of the term within the documentId
     */
    public void addTerm(String term, int documentId, int position) {
        if (index.containsKey(term)) {
            List<Posting> postings = index.get(term);
            if (postings.get(postings.size() - 1).getDocumentId() != documentId) {
                Posting p = new Posting(documentId);
                p.addPosition(position);
                postings.add(p);
            } else {
                postings.get(postings.size() - 1).addPosition(position);
            }

        } else {
            ArrayList<Posting> postings = new ArrayList<>();
            Posting p = new Posting(documentId);
            p.addPosition(position);
            postings.add(p);
            index.put(term, postings);
        }
    }

    @Override
    public void addToKGI(HashSet<String> types) {
        kgi.addToKGI(types);
    }

    @Override
    public String[] getWildcardMatches(String term) {
        return kgi.getWildcardMatches(term);
    }

    @Override
    public TokenProcessor getProcessor() {
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
}