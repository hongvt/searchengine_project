package cecs429.index;

import java.io.Serializable;
import java.util.*;

/**
 * A KGramIndex can retrieve types that match a wildcard search query
 * It is filled by creating k-grams of length 1-3 for every type that is passed into the method addToKGI
 */
public class KGramIndex implements Serializable {
    /**
     * Key is the kgram
     * Value is the list of vocab types that contain that kgram
     */
    private HashMap<String, HashSet<String>> kGramIndex;

    /**
     * Default Constructor that creates a KGramIndex
     * Instantiates the instance variable kGramIndex HashMap
     */
    public KGramIndex() {
        kGramIndex = new HashMap<String, HashSet<String>>();
    }

    /**
     * KGramIndexes the type parameter value
     *
     * @param type HashSet<String> - unique types to be added to the KGramIndex
     */
    public void addToKGI(HashSet<String> type) {

        //each element is unique because it was copied from the HashSet
        List<String> types = new ArrayList<>(type);

        for (int m = 0; m < types.size(); m++) {
            String temp = types.get(m);
            //add the 1-gram
            if (temp.length() == 1) {
                addKGramIndex(temp, temp);
            } else {
                //1-gram
                for (int j = 0; j < temp.length() - 1; j++) {
                    addKGramIndex(temp.substring(j, j + 1), temp);
                }
                //2,3-gram
                for (int j = 2; j <= 3; j++) {
                    getKGrams(j, temp);
                }
            }
        }
    }

    public HashSet<String> getTypesFromKGram(String key)
    {
        if (kGramIndex.containsKey(key)) {
            return kGramIndex.get(key);
        }
        return null;
    }



    /**
     * Adds the kgramKey parameter value to the kGramIndex if that key doesn't already exist and adds the type parameter value to the HashSet for that new Key
     * Adds the tyoe parameter value to the HashSet of the kgramKey in the kGramIndex if key already exists in the kGramIndex
     *
     * @param kgramKey String - kgram that is checked if it is contained in the kGramIndex
     * @param type     String - the type that is added to the key's HashSet
     */
    private void addKGramIndex(String kgramKey, String type) {
        if (!kGramIndex.containsKey(kgramKey)) {
            kGramIndex.put(kgramKey, new HashSet<String>());
        }
        kGramIndex.get(kgramKey).add(type);
    }

    /**
     * Splits the type parameter value into k-grams of length of len parameter value and calls addKGramIndex to check if the created kgram can be added to the kGramIndex
     *
     * @param len  int - 2 or 3 length of the kgram that is created
     * @param type String - the type that needs to be split in 2 or 3 grams
     */
    private void getKGrams(int len, String type) {
        String typez = "$" + type + "$";
        for (int i = 0; i <= typez.length() - len; i++) {
            String temp = typez.substring(i, i + len);
            addKGramIndex(temp, type);
        }
    }

    /**
     * Gets the types of the matches for the wildcard parameter value
     *
     * @param wildcard String - wildcard query (no spaces. ex. "hell*")
     * @return String[] - array of types that match the wildcard query
     */
    public String[] getWildcardMatches(String wildcard) {
        String temp = wildcard;
        boolean isLeading = false;
        String[] typesStems;

        //not a leading wildcard
        if (wildcard.charAt(0) != '*') {
            temp = "$" + wildcard;
        } else {
            isLeading = true;
        }
        if (wildcard.charAt(wildcard.length() - 1) != '*') {
            temp += "$";
        }

        String[] components = temp.split("\\*");
        ArrayList<String> grams = new ArrayList<>();
        ArrayList<String> andedVocab = new ArrayList<>();


        //gets the largest kgrams that can be created from each component and add to grams
        for (int i = 0; i < components.length; i++) {
            addKGramsToList(grams, components[i]);
        }

        //gets every list of types that match every kgram in grams and and the lists
        for (int i = 0; i < grams.size(); i++) {
            if (kGramIndex.containsKey(grams.get(i))) {
                HashSet<String> vocabTypes = kGramIndex.get(grams.get(i));
                if (i == 0)
                    andedVocab = andVocabTypes(true, andedVocab, vocabTypes);
                else
                    andedVocab = andVocabTypes(false, andedVocab, vocabTypes);
            }
        }

        //post-filtering
        ArrayList<Integer> removing = new ArrayList<>();

        if (!isLeading) {
            String leadingQ = components[0].substring(1);
            for (int i = 0; i < andedVocab.size(); i++) {
                if (andedVocab.get(i).length() >= leadingQ.length() && !andedVocab.get(i).substring(0, leadingQ.length()).equals(leadingQ)) {
                    removing.add(i);
                }
                if (andedVocab.get(i).length() < leadingQ.length()) {
                    removing.add(i);
                }
            }
        }

        //get the final list of words to get posts for
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
     * Ands the anded parameter value and the other parameter value
     * If it is the first iteration, then every element in the other parameter value should be added to the result list
     *
     * @param isFirstItr boolean - true if it is the first iteration for anding the lists of types that match each kgram, else false
     * @param anded      ArrayList<String> - the list of all the anded types
     * @param other      HashSet<String> - the next list of types to and
     * @return
     */
    private ArrayList<String> andVocabTypes(boolean isFirstItr, ArrayList<String> anded, HashSet<String> other) {
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> a = new ArrayList<>(other);

        if (isFirstItr && anded.size() == 0) {
            for (int i = 0; i < a.size(); i++) {
                result.add(a.get(i));
            }
        } else {
            for (int j = 0; j < anded.size(); j++) {
                for (int i = 0; i < a.size(); i++) {
                    if (anded.get(j).equals(a.get(i))) {
                        result.add(a.get(i));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets the largest kgram that can be created from the typeOrQuery parameter value and adds it to the list parameter value if it does not exist in that list
     *
     * @param list        ArrayList<String> - the list of largest kgrams that can be created from the wildcard
     * @param typeOrQuery String - a component of the wildcard, after the strip on the *
     */
    private void addKGramsToList(ArrayList<String> list, String typeOrQuery) {
        if (!typeOrQuery.equals("")) {
            if (typeOrQuery.length() == 1 || typeOrQuery.length() == 2 || typeOrQuery.length() == 3) {
                if (!list.contains(typeOrQuery)) {
                    list.add(typeOrQuery);
                }
            } else {
                for (int j = 0; j <= typeOrQuery.length() - 3; j++) {
                    if (!list.contains(typeOrQuery)) {
                        list.add(typeOrQuery.substring(j, j + 3));
                    }
                }
            }
        }
    }
}

