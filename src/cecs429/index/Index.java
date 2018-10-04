package cecs429.index;

import cecs429.text.TokenProcessor;
import java.util.HashSet;
import java.util.List;

/**
 * An Index can retrieve postings for a term from a data structure associating terms and the documents
 * that contain them.
 */
public interface Index {
    /**
     * Retrieves a list of Postings of documents that contain the given term.
     */
    List<Posting> getPostings(String term);

    /**
     * A (sorted) list of all terms in the index vocabulary.
     */
    List<String> getVocabulary();

    /**
     * Adds the parameter value to the KGramIndex associated with the Index
     *
     * @param types HashSet<String> - unique list of all the types within an Index
     */
    void addToKGI(HashSet<String> types);

    /**
     * Returns an array of types that match the wildcard
     *
     * @param term String - the wildcard
     * @return String[] - types that match the wildcard
     */
    String[] getWildcardMatches(String term);

    /**
     * Returns the !!!reference!!! of the TokenProcessor associated with the Index
     *
     * @return TokenProcessor used to index the Index and used to process the queries for the Index
     */
    TokenProcessor getProcessor();
}

