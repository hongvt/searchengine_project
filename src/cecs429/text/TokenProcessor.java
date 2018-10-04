package cecs429.text;

/**
 * A TokenProcessor applies some rules of normalization to a token from a document, and returns a term for that token.
 */
public interface TokenProcessor {
	/**
	 * Normalizes a token into a term.
	 */
	String processToken(String token);

	/**
	 * NORMALIZES the token parameter into TERM(S)
	 * @param token String - token without whitespace
	 * @return String[] - the length is 1 if there were no hyphens in the token parameter value; the terms
	 */
	String[] processTokens (String token);

	/**
	 * Gets the TYPES of the parameter value token
	 * Does not stem the token - NOT NORMALIZED
	 * @param token String - token without whitespace
	 * @return String[] - the length is 1 if there were no hyphens in the token parameter value; the types
	 */
	String[] processButDontStemTokensAKAGetType(String token);

	/**
	 * Gets the NORMALIZED TERMS of the parameter value types by stemming
	 * @param types String[] - an array of types
	 * @return String[] - an array of terms (normalized)
	 */
	String[] getStems(String[] types);

	/**
	 * Gets the parameter value token half processed (NOT A TYPE YET)
	 * @param token String - token without whitespace
	 * @return String - the half processed token aka just removed the non-alphanumeric characters in the beginning and end and all quotes and apostrophes anywhere in the string
	 */
	String removeNonAlphaNumCharBegEndAndQuotes(String token);

	/**
	 * Gets the NORMALIZED TERM of the parameter value type by stemming
	 * @param type String - the type that gets stemmed to become a normalized term
	 * @return String - the normalized/stemmed term
	 */
	String getStem(String type);
}
