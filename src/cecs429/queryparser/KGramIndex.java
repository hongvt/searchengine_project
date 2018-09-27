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
    public List<Posting> getPostings(Index index)
    {
        ArrayList<Posting> orPosts = new ArrayList<>();
        String temp = wildcard;
        boolean isTrailingONE = false;
        boolean isLeading = false;
        boolean isTrailing = false;
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
            isTrailing = true;
        }
        String[] components = temp.split("\\*");
        ArrayList<String> grams = new ArrayList<>();
        List<String> vocabTypes = index.getVocabularyTypes();
        ArrayList<String> andedVocabTypes = new ArrayList<>();

        for (int i = 0; i < components.length; i++)
        {
            //System.out.println("component["+i+"]: "+ components[i]);
            addKGramsToList(grams, components[i]);
        }
        System.out.print("vocabTypes: ");
        System.out.println(vocabTypes);
        for(int i = 0; i < vocabTypes.size(); i++)
        {
            for (int j = 0; j < grams.size(); j++)
            {
                //System.out.println(vocabTypes);
                String tempGram = grams.get(j);
                if (grams.get(j).contains("$"))
                {
                    if (tempGram.charAt(0) == '$')
                    {
                        tempGram = grams.get(j).substring(1);
                        if (vocabTypes.get(i).length() >= tempGram.length())
                            //System.out.println(vocabTypes.get(i).substring(0,tempGram.length()));
                        if (vocabTypes.get(i).length() >= tempGram.length() && vocabTypes.get(i).substring(0,tempGram.length()).equals(tempGram))
                        {
                            if (!kGramIndex.containsKey(grams.get(j)))
                            {
                                kGramIndex.put(grams.get(j), new ArrayList<>());
                            }
                            kGramIndex.get(grams.get(j)).add(vocabTypes.get(i));
                        }
                    }
                    else if (tempGram.charAt(tempGram.length()-1) == '$')
                    {
                        tempGram = grams.get(j).substring(0,grams.get(j).length()-1);
                        if (vocabTypes.get(i).substring(vocabTypes.get(i).length()-tempGram.length()).equals(tempGram))
                        {
                            if (!kGramIndex.containsKey(tempGram))
                            {
                                kGramIndex.put(grams.get(j), new ArrayList<>());
                            }
                            kGramIndex.get(grams.get(j)).add(vocabTypes.get(i));
                        }
                    }
                    //System.out.println("tempGram: " + tempGram);

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
        System.out.print("kGramIndex: ");
        System.out.println(kGramIndex);

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
        System.out.print("andedVocabTypes: ");
        System.out.println(andedVocabTypes);
        if (isTrailingONE && !isLeading)
        {
            String leadingQ = temp.substring(1,temp.length()-1);
            System.out.println("leadingQ: "+leadingQ);
            for (int i = 0; i < andedVocabTypes.size(); i++)
            {
                if (!andedVocabTypes.get(i).substring(0,leadingQ.length()).equals(leadingQ))
                {
                    andedVocabTypes.remove(i);
                }
            }
        }

        System.out.print("andedVocabTypes: ");
        System.out.println(andedVocabTypes);

        typesStems = new String[andedVocabTypes.size()];

        for (int i = 0; i < andedVocabTypes.size(); i++)
        {
            typesStems[i] = andedVocabTypes.get(i);
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
        System.out.print("orPosts: ");
        System.out.println(orPosts);
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
                if (anded.isEmpty() || !anded.contains(smaller.get(i)))
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
                        System.out.println("adding: " + typeOrQuery.substring(j, j + 3));
                        list.add(typeOrQuery.substring(j, j + 3));
                    }
                }
            }
        }
    }
}
