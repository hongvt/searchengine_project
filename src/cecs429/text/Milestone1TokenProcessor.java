package cecs429.text;

import libstemmer_java.java.org.tartarus.snowball.SnowballStemmer;
import libstemmer_java.java.org.tartarus.snowball.ext.englishStemmer;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Milestone1TokenProcessor creates terms from tokens by removing all non-alphanumeric characters at the Beginning and the End of the token and converting it to all lowercase.
 * Splits hyphenated words into separate terms and a single combined term
 * Uses the Porter2 stemmer implementation to stem the token
 * The token must not have any whitespace
 */
public class Milestone1TokenProcessor implements TokenProcessor {

    @Override
    public String processToken(String token) {
        return token.replaceAll("\\W", "").toLowerCase();
    }

    @Override
    public String[] processButDontStemTokensAKAGetType(String token) {
        ArrayList<String> finalWords = new ArrayList<String>();

        String[] terms = token.split("-");
        //no hyphens
        if (terms.length == 1) {
            String temp = removeNonAlphaNumCharBegEndAndQuotes(token).toLowerCase();
            finalWords.add(temp);
        } else {
            String combined = "";
            for (int i = 0; i < terms.length; i++) {
                if (terms[i].length() != 0) //word
                {
                    combined += terms[i];
                    String temp = removeNonAlphaNumCharBegEndAndQuotes(terms[i]).toLowerCase();
                    finalWords.add(temp);
                }
            }
            String temp = removeNonAlphaNumCharBegEndAndQuotes(combined).toLowerCase();
            finalWords.add(temp);
        }
        String[] words = new String[finalWords.size()];
        for (int i = 0; i < finalWords.size(); i++) {
            words[i] = finalWords.get(i);
        }
        return words;
    }

    @Override
    public String[] processTokens(String token) {
        String[] words = processButDontStemTokensAKAGetType(token);
        return getStems(words);
    }

    @Override
    public String removeNonAlphaNumCharBegEndAndQuotes(String token) {
        Pattern p = Pattern.compile("(\\w+)((\\W+)(\\w+))*");
        Matcher m = p.matcher(token);
        if (m.find()) {
            return m.group(0).replace("\"", "").replace("\'", "");
        }
        return "";
    }

    @Override
    public String[] getStems(String[] types) {
        String[] stems = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            stems[i] = getStem(types[i]);
        }
        return stems;
    }

    @Override
    public String getStem(String type) {
        SnowballStemmer snowballStemmer = new englishStemmer();
        snowballStemmer.setCurrent(type.toLowerCase());
        snowballStemmer.stem();
        return snowballStemmer.getCurrent();
    }
}


