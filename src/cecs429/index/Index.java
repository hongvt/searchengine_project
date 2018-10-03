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

    void addToKGI(HashSet<String> types);

    String[] getWildcardMatches(String term);

    TokenProcessor getProcessor();
}

