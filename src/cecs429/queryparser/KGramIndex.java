package cecs429.queryparser;


import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.Milestone1TokenProcessor;
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
    public List<Posting> getPostings(Index index)
    {
        ArrayList<Posting> orPosts = new ArrayList<>();
        String temp = wildcard;
        boolean isTrailingONE = false;
        boolean isLeading = false;
        String[] typesStems;

        if (wildcard.charAt(0) != '*') //not a leading wildcard
        {
            temp = "$" + wildcard;
        }
        else
        {
            isLeading = true;
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
        List<String> andedVocabTypes = new ArrayList<>();

        for (int i = 0; i < components.length; i++)
        {
            addKGramsToList(grams, components[i]);
        }
        for(int i = 0; i < vocabTypes.size(); i++)
        {
            for (int j = 0; j < grams.size(); j++)
            {
                String tempGram = grams.get(j);
                if (grams.get(j).contains("$"))
                {
                    if (tempGram.charAt(0) == '$')
                    {
                        tempGram = grams.get(j).substring(1);
                        if (vocabTypes.get(i).length() >= tempGram.length())
<<<<<<< HEAD
                            if (vocabTypes.get(i).length() >= tempGram.length() && vocabTypes.get(i).substring(0,tempGram.length()).equals(tempGram))
                            {
                                if (!kGramIndex.containsKey(grams.get(j)))
                                {
                                    kGramIndex.put(grams.get(j), new ArrayList<>());
                                }
                                kGramIndex.get(grams.get(j)).add(vocabTypes.get(i));
                            }
=======
                        if (vocabTypes.get(i).length() >= tempGram.length() && vocabTypes.get(i).substring(0,tempGram.length()).equals(tempGram))
                        {
                            if (!kGramIndex.containsKey(grams.get(j)))
                            {
                                kGramIndex.put(grams.get(j), new ArrayList<>());
                            }
                            kGramIndex.get(grams.get(j)).add(vocabTypes.get(i));
                        }
>>>>>>> 5556e44a38bb952076adfe65cbade427a449f1d7
                    }
                    else if (tempGram.charAt(tempGram.length()-1) == '$')
                    {
                        tempGram = grams.get(j).substring(0,grams.get(j).length()-1);
                        if (vocabTypes.get(i).length() >= tempGram.length() && vocabTypes.get(i).substring(vocabTypes.get(i).length()-tempGram.length()).equals(tempGram))
                        {
                            if (!kGramIndex.containsKey(grams.get(j)))
                            {
                                kGramIndex.put(grams.get(j), new ArrayList<>());
                            }
                            kGramIndex.get(grams.get(j)).add(vocabTypes.get(i));
                        }
                    }


                }
                else
                {
                    if (vocabTypes.get(i).contains(tempGram))
                    {
                        if (!kGramIndex.containsKey(tempGram))
                        {
                            kGramIndex.put(grams.get(j), new ArrayList<>());
                        }
                        kGramIndex.get(grams.get(j)).add(vocabTypes.get(i));
                    }
                }

            }
        }
        andedVocabTypes.addAll(kGramIndex.get(grams.get(0)));
        if (grams.size() == 2)
        {
            andedVocabTypes = andVocabTypes(andedVocabTypes, kGramIndex.get(grams.get(1)));
        }
        else if (grams.size() > 2)
        {
            for (int i = 1; i < grams.size(); i++)
            {
                andedVocabTypes = andVocabTypes(andedVocabTypes, kGramIndex.get(grams.get(i)));
            }
        }

        ArrayList<Integer> removing = new ArrayList<>();

        if (isTrailingONE && !isLeading)
        {
            String leadingQ = temp.substring(1,temp.length()-1);

            for (int i = 0; i < andedVocabTypes.size(); i++)
            {
                if (!andedVocabTypes.get(i).substring(0,leadingQ.length()).equals(leadingQ))
                {
                    removing.add(i);
                }
            }
        }
        System.out.print("Matches for words: ");
        typesStems = new String[andedVocabTypes.size()-removing.size()];
        int k = 0;
        for (int i = 0; i < andedVocabTypes.size(); i++)
        {
            if (!removing.contains(i)) {
                typesStems[k] = andedVocabTypes.get(i);
                if (k <andedVocabTypes.size()-removing.size()-1)
                    System.out.print(typesStems[k]+",");
                else
                    System.out.println(typesStems[k]);
                k++;
            }
        }

        typesStems = (new Milestone1TokenProcessor()).getStems(typesStems);

        for (int i = 0; i < typesStems.length; i++)
        {
            for (int j = 0; j < index.getPostings(typesStems[i]).size(); j++)
            {
                if (orPosts.isEmpty() || !orPosts.contains(index.getPostings(typesStems[i]).get(j)))
                {
                    orPosts.add(index.getPostings(typesStems[i]).get(j));
                }
            }
        }
        return orPosts;
    }
    private List<String> andVocabTypes(List<String> anded, List<String> a)
    {
        ArrayList<String> result = new ArrayList<>();
        List<String> small = anded.size() <= a.size() ? anded : a;
        List<String> big = anded.size() > a.size() ? anded : a;

        for (int i = 0; i < small.size(); i++)
        {
            if (big.contains(small.get(i)))
            {
                result.add(small.get(i));
            }
        }
        return result;
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
<<<<<<< HEAD
}
=======
}
>>>>>>> 5556e44a38bb952076adfe65cbade427a449f1d7
