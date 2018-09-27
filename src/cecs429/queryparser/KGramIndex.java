package cecs429.queryparser;


import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.Milestone1TokenProcessor;
import cecs429.text.TokenProcessor;
import java.util.*;

public class KGramIndex implements QueryComponent {
    /**
     * Key is the kgram
     * Value is the list of vocab types that match that Key kgram
     */
    private HashMap<String, List<String>> kGramIndex;
    private String wildcard;

    /**
     *
     * @param term NO WHITESPACE, wildcard
     *             example hell*
     */
    public KGramIndex (String term)
    {
        wildcard = term;
        kGramIndex = new HashMap<String, List<String>>();
    }

    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor)
    {
        ArrayList<Posting> orPosts = new ArrayList<>();
        String temp = wildcard;
        boolean isTrailingONE = false;
        String[] typesStems;

        if (wildcard.charAt(0) != '*') //not a leading wildcard
        {
            temp = "$" + wildcard;
        }
        if (wildcard.charAt(wildcard.length()-1) != '*') //not a trailing wildcard
        {
            temp += "$";
        }
        else
        {
            int counter = 0;
            for( int i=0; i<wildcard.length(); i++ ) {
                if( wildcard.charAt(i) == '*' ) {
                    counter++;
                }
            }
            if (counter == 1)
            {
                isTrailingONE = true;
            }
        }
        String[] components = temp.split("\\*");
        ArrayList<String> grams = new ArrayList<>();
        List<String> vocabTypes = index.getVocabularyTypes();
        ArrayList<String> andedVocabTypes = new ArrayList<>();

        for (int i = 0; i < components.length; i++)
        {
            addKGramsToList(grams, components[i]);
        }

        for(int i = 0; i < vocabTypes.size(); i++)
        {
            for (int j = 0; j < grams.size(); j++)
            {
                if (vocabTypes.get(i).contains(grams.get(j)))
                {
                    if (!kGramIndex.containsKey(grams.get(j)))
                    {
                        kGramIndex.put(grams.get(j), new ArrayList<>());
                    }
                    kGramIndex.get(grams.get(j)).add(vocabTypes.get(i));
                }
            }
        }

        if (grams.size() == 1)
        {
            andedVocabTypes.addAll(kGramIndex.get(grams.get(0)));
        }
        else if (grams.size() == 2)
        {
            andVocabTypes(kGramIndex.get(grams.get(0)), kGramIndex.get(grams.get(1)), andedVocabTypes);
        }
        else
        {
            for (int i = 0; i < grams.size()-1; i++)
            {
                andVocabTypes(kGramIndex.get(grams.get(i)), kGramIndex.get(grams.get(i+1)), andedVocabTypes);
            }
        }

        if (isTrailingONE)
        {
            String leadingQ = temp.substring(0,temp.length()-1);
            for (int i = 0; i < andedVocabTypes.size(); i++)
            {
                if (!andedVocabTypes.get(i).contains(leadingQ))
                {
                    andedVocabTypes.remove(i);
                }
            }
        }

        typesStems = new String[andedVocabTypes.size()];

        for (int i = 0; i < andedVocabTypes.size(); i++)
        {
            typesStems[i] = andedVocabTypes.get(i).substring(1,andedVocabTypes.get(i).length()-1);
        }

        typesStems = (new Milestone1TokenProcessor()).getStems(typesStems);

        for (int i = 0; i < typesStems.length; i++)
        {
            for (int j = 0; j < index.getPostings(typesStems[i]).size(); j++)
            {
                if (!orPosts.contains(index.getPostings(typesStems[i]).get(i)))
                {
                    orPosts.add(index.getPostings(typesStems[i]).get(i));
                }
            }
        }
        return orPosts;
    }
    private void andVocabTypes(List<String> a, List<String> b, ArrayList<String> anded)
    {
        List<String> bigger = a.size() > b.size() ? a : b;
        List<String> smaller = a.size() <= b.size() ? a : b;
        for (int i = 0; i < smaller.size(); i++)
        {
            if (bigger.contains(smaller.get(i)))
            {
                if (!anded.contains(smaller.get(i)))
                {
                    anded.add(smaller.get(i));
                }
            }
        }
    }

    /**
     *
     * @param list
     * @param typeOrQuery HAS the $
     *                    example: $red*
     *                    $re red
     */
    private void addKGramsToList(ArrayList<String> list, String typeOrQuery)
    {
        if (!typeOrQuery.equals("")) {
            if (typeOrQuery.length() == 1 || typeOrQuery.length() == 2 || typeOrQuery.length() == 3) {
                if (!list.contains(typeOrQuery)) {
                    list.add(typeOrQuery);
                }
            } else {
                for (int j = 0; j < typeOrQuery.length() - 2; j++) {
                    if (!list.contains(typeOrQuery)) {
                        list.add(typeOrQuery.substring(j, j + 3));
                    }
                }
            }
        }
    }
}
