package cecs429.queryparser;

import java.util.*;

public class KGramIndex {
    /**
     * Key is the kgram
     * Value is the list of vocab types that contain that kgram
     */
    private HashMap<String, List<String>> kGramIndex;

    /**
     *
     */
    public KGramIndex() {
        kGramIndex = new HashMap<String, List<String>>();
    }

    /**
     * @param type
     */
    public void addToKGI(HashSet<String> type) {

        List<String> types = new ArrayList<>(type);

        for (int m = 0; m < types.size(); m++) {
            String temp = types.get(m);
            if (temp.length() == 1) //add the 1-gram
            {
                addKGramIndex(temp, temp);
            } else {
                //1-gram
                for (int j = 0; j < temp.length() - 1; j++) {
                    addKGramIndex(temp.substring(j, j + 1), temp);
                }
                //2,3-gram
                String typez = "$" + temp + "$";
                for (int j = 2; j <= 3; j++) {
                    ArrayList<String> kgrams = getKGrams(j, typez);
                    for (int k = 0; k < kgrams.size(); k++) {
                        addKGramIndex(kgrams.get(k), temp);
                    }
                }
            }
        }
    }


    /**
     * @param kgramKey
     * @param type
     */
    private void addKGramIndex(String kgramKey, String type) {
        if (!kGramIndex.containsKey(kgramKey)) {
            kGramIndex.put(kgramKey, new ArrayList<String>());
            kGramIndex.get(kgramKey).add(type);
        } else {
            if (!kGramIndex.get(kgramKey).contains(type)) {
                kGramIndex.get(kgramKey).add(type);
            }
        }
    }

    /**
     * @param len  - 2,3
     * @param type
     */
    private ArrayList<String> getKGrams(int len, String type) {
        ArrayList<String> kgrams = new ArrayList<String>();
        for (int i = 0; i <= type.length() - len; i++) {
            String temp = type.substring(i, i + len);
            if (kgrams.isEmpty() || !kgrams.contains(temp)) {
                kgrams.add(temp);
            }
        }
        return kgrams;
    }

    /**
     * @param wildcard
     * @return
     */
    public String[] getWildcardMatches(String wildcard) {
        String temp = wildcard;
        boolean isTrailingONE = false;
        boolean isLeading = false;
        String[] typesStems;

        if (wildcard.charAt(0) != '*') //not a leading wildcard
        {
            temp = "$" + wildcard;
        } else {
            isLeading = true;
        }
        if (wildcard.charAt(wildcard.length() - 1) != '*') //not a trailing wildcard
        {
            temp += "$";
        } else {
            int counter = 0;
            for (int i = 0; i < wildcard.length(); i++) {
                if (wildcard.charAt(i) == '*') {
                    counter++;
                }
            }
            if (counter == 1) {
                isTrailingONE = true;
            }
        }
        String[] components = temp.split("\\*");
        ArrayList<String> grams = new ArrayList<>();
        ArrayList<String> andedVocab = new ArrayList<>();


        for (int i = 0; i < components.length; i++) {
            addKGramsToList(grams, components[i]);
        }

        for (int i = 0; i < grams.size(); i++) {
            if (kGramIndex.containsKey(grams.get(i))) {
                List<String> vocabTypes = kGramIndex.get(grams.get(i));
                andedVocab = andVocabTypes(andedVocab, vocabTypes);
            }
        }

        ArrayList<Integer> removing = new ArrayList<>();

        if (isTrailingONE && !isLeading) {
            String leadingQ = temp.substring(1, temp.length() - 1);

            for (int i = 0; i < andedVocab.size(); i++) {
                if (!andedVocab.get(i).substring(0, leadingQ.length()).equals(leadingQ)) {
                    removing.add(i);
                }
            }
        }
        System.out.print("Matches for words: ");
        typesStems = new String[andedVocab.size() - removing.size()];
        int k = 0;
        for (int i = 0; i < andedVocab.size(); i++) {
            if (!removing.contains(i)) {
                typesStems[k] = andedVocab.get(i);
                if (k < andedVocab.size() - removing.size() - 1)
                    System.out.print(typesStems[k] + ",");
                else
                    System.out.println(typesStems[k]);
                k++;
            }
        }
        return typesStems;
    }

    /**
     * @param anded
     * @param a
     * @return
     */
    private ArrayList<String> andVocabTypes(ArrayList<String> anded, List<String> a) {
        ArrayList<String> result = new ArrayList<>();
        List<String> small = anded.size() <= a.size() ? anded : a;
        List<String> big = anded.size() > a.size() ? anded : a;

        if (anded.size() == 0) {
            for (int i = 0; i < a.size(); i++) {
                result.add(a.get(i));
            }
        } else {
            for (int i = 0; i < small.size(); i++) {
                if (big.contains(small.get(i))) {
                    result.add(small.get(i));
                }
            }
        }
        return result;
    }

    /**
     * @param list
     * @param typeOrQuery HAS the $
     *                    example: $red*
     *                    $re red
     */
    private void addKGramsToList(ArrayList<String> list, String typeOrQuery) {
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

