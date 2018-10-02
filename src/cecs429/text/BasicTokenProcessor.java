package cecs429.text;

/**
 * A BasicTokenProcessor creates terms from tokens by removing all non-alphanumeric characters from the token, and
 * converting it to all lowercase.
 */
public class BasicTokenProcessor implements TokenProcessor {
	@Override
	public String processToken(String token) {
		return token.replaceAll("\\W", "").toLowerCase();
	}

	@Override
	public String[] processTokens(String token) {
		return new String[] {token};
	}

	@Override
	public String[] processButDontStemTokensAKAGetType(String token) {
		return new String[0];
	}

	@Override
	public String[] getStems(String[] tokens) {
		return new String[0];
	}

	@Override
	public String removeNonAlphaNumCharBegEndAndQuotes(String term) {
		return null;
	}

	@Override
	public String getStem(String token) {
		return null;
	}
}