package cecs429.queryparser;


import cecs429.index.Index;
import cecs429.index.Posting;
import javafx.geometry.Pos;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KGramIndex implements QueryComponent
{
    /**
     * Key is the kgram
     * Value is the list of vocab words that match that Key kgram
     */
    private HashMap<String, List<String>> kGramIndex;

    private class KGram
    {
        /**
         * mTerm is the original String
         */
        private String mTerm;
        /**
         * kGrams is a list of a kGrams from the original String
         */
        private List<String> kGrams;
        /**
         * k is the size of the kGram
         */
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

                }
                if (mTerm.charAt(mTerm.length()-1) != '*') //not a trailing wildcard
                {

                }

            }
            return new ArrayList<String>(kGrams);
        }
    }

    public KGramIndex (String term)
    {
        kGramIndex = new HashMap<String, List<String>>();
        KGram gram = new KGram(term);

        for (int i = 0; i < gram.getKGrams().size(); i++)
        {
            kGramIndex.put(gram.getKGrams().get(i), new ArrayList<String>());
        }
    }


    @Override
    public List<Posting> getPostings(Index index)
    {
        List<Posting> posts = new ArrayList<Posting>();
        List<String> vocab = index.getVocabulary();
        for(int i = 0; i < vocab.size(); i++)
        {
            //KGram temp = new KGram(vocab.get(i),k);
            KGram temp = new KGram(vocab.get(i));
            for (int j = 0; j < temp.getKGrams().size(); j++)
            {
                if (kGramIndex.containsKey(temp.getKGrams().get(i)))
                {
                    kGramIndex.get(temp.getKGrams().get(i)).add(vocab.get(i));
                }
            }
        }

        for(int i = 0; i < kGramIndex.size()-1; i++)
        {
            ArrayList<String> anded = new ArrayList<>();

        }



        return null;
    }

    private List<String> getIntersectVocab(List<String> a, List<String> b)
    {
        ArrayList<String> anded = new ArrayList<>();

        for(int i = 0; i < a.size(); i++)
        {
            if (b.contains(a.get(i)))
            {
                anded.add(a.get(i));
            }
        }
        return anded;
    }
}
