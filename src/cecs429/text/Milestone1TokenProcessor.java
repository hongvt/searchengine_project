package cecs429.text;

import libstemmer_java.java.org.tartarus.snowball.SnowballStemmer;
import libstemmer_java.java.org.tartarus.snowball.ext.englishStemmer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A BasicTokenProcessor creates terms from tokens by removing all non-alphanumeric characters from the token, and
 * converting it to all lowercase.
 */
public class Milestone1TokenProcessor implements TokenProcessor {
    @Override
    public String processToken(String token)
    {
        return token.replaceAll("\\W", "").toLowerCase();
    }

    @Override
    public String[] processTokens(String token)
    {
        Pattern p = Pattern.compile("(\\w+)((\\W+)(\\w+))*");
        Matcher m = p.matcher(token);
        while (m.find()) {
            String term = m.group(0).replaceAll("\"\'","").toLowerCase();
            String[] terms = term.split("-");
            if (terms.length == 1)
            {
                return getStems(terms);
            }
            else
            {
                String[] termsHyphen = new String[terms.length+1];
                String combined = "";
                for(int i = 0; i < terms.length; i++)
                {
                    termsHyphen[i] = terms[i];
                    combined += terms[i];
                }
                termsHyphen[termsHyphen.length-1] = combined;
                return getStems(termsHyphen);
            }
        }
        return new String[] {token};
    }

    private String[] getStems(String[] tokens)
    {
        SnowballStemmer snowballStemmer = new englishStemmer();
        String[] stems = new String[tokens.length];
        for(int i = 0; i < tokens.length; i++)
        {
            snowballStemmer.setCurrent(tokens[i]);
            snowballStemmer.stem();
            stems[i] = snowballStemmer.getCurrent();
        }
        return stems;
    }
}


