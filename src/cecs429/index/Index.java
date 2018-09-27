package cecs429.index;

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

	List<String> getVocabularyTypes();
<<<<<<< HEAD
}
=======
}
>>>>>>> 5556e44a38bb952076adfe65cbade427a449f1d7
