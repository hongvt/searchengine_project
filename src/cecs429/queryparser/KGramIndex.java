package cecs429.queryparser;


import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.*;

public class KGramIndex implements QueryComponent {
    /**
     * Key is the kgram
     * Value is the list of vocab words that match that Key kgram
     */
    private HashMap<String, List<String>> kGramIndex;
    private String wildcard;

    /*private class KGram
    {
        private String mTerm;
        private List<String> kGrams;
        private int k;

        public KGram(String term, int num) //not a wildcard
        {
            mTerm = term;
            k = num;
            kGrams = new ArrayList<>();
        }

        public KGram(String term) //wildcard
        {
            mTerm = term;
            kGrams = new ArrayList<>();
            k = 0;
        }

        public String getTerm()
        {
            return mTerm;
        }

        public int getK()
        {
            return k;
        }

        public List<String> getKGrams()
        {
            if (k != 0) //not a wildcard
            {
                String temp = "$" + mTerm + "$";
                for (int i = 0; i < temp.length() - k - 1; i++)
                {
                    kGrams.add(temp.substring(i, i + k));
                }
            }
            else //wildcard
            {
                String temp = mTerm;
                if (mTerm.charAt(0) != '*') //not a leading wildcard
                {
                    temp = "";
                    temp = "$" + mTerm;
                }
                if (mTerm.charAt(mTerm.length()-1) != '*') //not a trailing wildcard
                {
                    temp += "$";
                }
                String[] grams = temp.split("\\*");
                for (int i = 0; i < grams.length; i++)
                {
                    kGrams.add(grams[i]);
                }
            }
            return new ArrayList<String>(kGrams);
        }
    }*/

    public KGramIndex(String term) {
        wildcard = term;
        kGramIndex = new HashMap<String, List<String>>();
    }


    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        String temp = "$" + wildcard + "$";
        String[] components = temp.split("\\*");
        ArrayList<String> grams = new ArrayList<>();

        for (int i = 0; i < components.length; i++) {
            if (!components[i].equals("")) {
                if (components[i].length() == 1 || components[i].length() == 2 || components[i].length() == 3) {
                    grams.add(components[i]);
                } else {
                    for (int j = 0; j < components[i].length() - 2; j++) {
                        grams.add(components[i].substring(j, j + 3));
                    }
                }
            }
        }


        return null;
    }

}
