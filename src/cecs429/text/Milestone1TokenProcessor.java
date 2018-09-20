package cecs429.text;

import libstemmer_java.java.org.tartarus.snowball.SnowballStemmer;
import libstemmer_java.java.org.tartarus.snowball.ext.englishStemmer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        ArrayList<String> finalWords = new ArrayList<String>();
        String term = removeNonAlphaNumCharBegEndAndQuotes(token);
        String[] terms = term.split("-");
        if (terms.length == 1) //no hyphens
        {
            String temp = removeNonAlphaNumCharBegEndAndQuotes(terms[0]).toLowerCase();
            if (!temp.equals(""))
            {
                finalWords.add(removeNonAlphaNumCharBegEndAndQuotes(terms[0]).toLowerCase());
            }
        }
        else
        {
            boolean extraHyphen = false;
            String combined = "";
            for (int i = 0; i < terms.length; i++)
            {
                if (terms[i].length() != 0) //word
                {
                    if (!extraHyphen)
                    {
                        combined += terms[i];
                    }
                    String temp = removeNonAlphaNumCharBegEndAndQuotes(terms[i]).toLowerCase();
                    if (!temp.equals(""))
                    {
                        finalWords.add(removeNonAlphaNumCharBegEndAndQuotes(terms[i]).toLowerCase());
                    }
                }
                else //extra hyphen
                {
                    if (extraHyphen)
                    {
                        combined = "";
                        extraHyphen = false;
                    }
                    else
                    {
                        if (!finalWords.contains(combined))
                        {
                            String temp = removeNonAlphaNumCharBegEndAndQuotes(combined).toLowerCase();
                            if (!temp.equals(""))
                            {
                                finalWords.add(removeNonAlphaNumCharBegEndAndQuotes(combined).toLowerCase());
                            }
                            combined = "";
                        }
                        extraHyphen = true;
                    }
                }
            }
            if (!combined.equals(""))
            {
                String temp = removeNonAlphaNumCharBegEndAndQuotes(combined).toLowerCase();
                if (!temp.equals(""))
                {
                    finalWords.add(removeNonAlphaNumCharBegEndAndQuotes(combined).toLowerCase());
                }
            }
        }
        String[] words = new String[finalWords.size()];
        for (int i = 0; i < finalWords.size(); i++)
        {
            words[i] = finalWords.get(i);
        }
        return getStems(words);
    }

    private String removeNonAlphaNumCharBegEndAndQuotes(String term)
    {
        Pattern p = Pattern.compile("(\\w+)((\\W+)(\\w+))*");
        Matcher m = p.matcher(term);
        if (m.find())
        {
            return m.group(0).replace("\"", "").replace("\'", "");
        }
        return "";

    }


    private String[] getStems(String[] tokens)
    {
        SnowballStemmer snowballStemmer = new englishStemmer();
        String[] stems = new String[tokens.length];
        for(int i = 0; i < tokens.length; i++)
        {
            snowballStemmer.setCurrent(tokens[i].toLowerCase());
            snowballStemmer.stem();
            stems[i] = snowballStemmer.getCurrent();
        }
        return stems;
    }
}


