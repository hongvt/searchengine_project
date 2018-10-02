package cecs429.text;

/**
 * A TokenProcessor applies some rules of normalization to a token from a document, and returns a term for that token.
 */
public interface TokenProcessor {
	/**
	 * Normalizes a token into a term.
	 */
	String processToken(String token);
	String[] processTokens (String token);
	String[] processButDontStemTokensAKAGetType(String token);
	String[] getStems(String[] tokens);
	String removeNonAlphaNumCharBegEndAndQuotes(String term);
	String getStem(String token);
}
