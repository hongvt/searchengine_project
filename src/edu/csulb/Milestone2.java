package edu.csulb;

import cecs429.documents.*;
import cecs429.index.*;
import cecs429.queryparser.BooleanQueryParser;
import cecs429.queryparser.QueryComponent;
import cecs429.text.EnglishTokenStream;
import cecs429.text.Milestone1TokenProcessor;
import cecs429.text.TokenProcessor;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Contains the main for Milestone 2
 */
public class Milestone2 {

    /**
     *
     * @param index
     * @param misspelledTerm
     */
    private static void spellingCorrection(DiskPositionalIndex index, String misspelledTerm) {
        String[] misspelledterms = misspelledTerm.split(" ");
        HashSet<String> misspelledTermKG = new HashSet<>();

        //get the k-grams for the misspelledTerm
        for (int i = 0; i < misspelledterms.length; i++) {
            misspelledTermKG.addAll(getKGrams(misspelledterms[i]));
        }

        //get all the vocab types that share the same k-grams with the misspelledTerm
        HashSet<String> types = new HashSet<>();
        for (String kg : misspelledTermKG) {
            HashSet<String> typesTemp = index.getKGramIndex().getTypesFromKGram(kg);
            if (typesTemp != null) {
                types.addAll(index.getKGramIndex().getTypesFromKGram(kg));
            }
        }

        //calculate jaccard coef for each type
        HashMap<String, Double> typeJaccard = new HashMap<>();
        for (String type : types) {
            ArrayList<String> typeKG = getKGrams(type);
            double kgCommon = 0;
            for (int i = 0; i < typeKG.size(); i++) {
                if (misspelledTermKG.contains(typeKG.get(i))) {
                    kgCommon++;
                }
            }
            double kgDistinct = typeKG.size() + misspelledTermKG.size() - kgCommon;
            typeJaccard.put(type, kgCommon / kgDistinct);
        }

        //get all the types that have a jaccard coef higher than threshold (0.22)
        //and get the edit distance for them between the misspelled term
        //System.out.println("meet 0.22 jaccard coef:");
        HashMap<String, Integer> typeEditDist = new HashMap<>();
        for (String type : typeJaccard.keySet()) {
            if (typeJaccard.get(type) > 0.22) {
                typeEditDist.put(type, getEditDistance(misspelledTerm.replaceAll(" ", ""), type));
                //System.out.println(type+":"+);
            }
        }

        //get the type with the lowest edit distance
        int min = 1000;
        for (String type : typeEditDist.keySet()) {
            if (typeEditDist.get(type) < min) {
                min = typeEditDist.get(type);
            }
        }

        //if multiple types' edit distance tie for lowest, get the type with the highest doc frequency
        //System.out.println(typeEditDist);
        int maxDf = -1;
        for (String type : typeEditDist.keySet()) {
            if (typeEditDist.get(type) == min) {
                //System.out.println(type);
                int postingsLength = (index.getPostingsNoPositions(index.getProcessor().getStem(type))).length;
                if (postingsLength > maxDf) {
                    maxDf = postingsLength;
                }
            }
        }

        System.out.println("Search instead for: ");
        for (String type : typeEditDist.keySet()) {
            if (typeEditDist.get(type) == min) {
                if ((index.getPostingsNoPositions(index.getProcessor().getStem(type))).length == maxDf) {
                    System.out.println(type);
                }
            }
        }
    }

    /**
     *
     * @param term
     * @param candidate
     * @return
     */
    private static int getEditDistance(String term, String candidate) {
        int[][] dp = new int[term.length()][candidate.length()];
        for (int i = 0; i < term.length(); i++) {
            for (int j = 0; j < candidate.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int topRight = dp[i - 1][j] + 1;
                    int botLeft = dp[i][j - 1] + 1;
                    int topLeft = dp[i - 1][j - 1] + (term.charAt(i - 1) == candidate.charAt(j - 1) ? 0 : 1);

                    int temp = Math.min(topRight, botLeft);
                    dp[i][j] = Math.min(temp, topLeft);
                }
            }
        }
        return dp[term.length() - 1][candidate.length() - 1];
    }

    /**
     *
     * @param term
     * @return
     */
    private static ArrayList<String> getKGrams(String term) {
        ArrayList<String> kg = new ArrayList<>();
        //add the 1-gram
        if (term.length() == 1) {
            kg.add(term);
        } else {
            //1-gram
            for (int j = 0; j < term.length() - 1; j++) {
                kg.add(term.substring(j, j + 1));
            }
            //2,3-gram
            for (int j = 2; j <= 3; j++) {
                String typez = "$" + term + "$";
                for (int k = 0; k <= typez.length() - j; k++) {
                    kg.add(typez.substring(k, k + j));
                }
            }
        }
        return kg;
    }

    /**
     *
     * @param diskPosIndex
     * @param word
     * @param corpus
     * @param keyboard
     */
    private static void booleanQueryIndex(DiskPositionalIndex diskPosIndex, String word, DocumentCorpus corpus, Scanner keyboard) {
        BooleanQueryParser pa = new BooleanQueryParser();
        QueryComponent c = pa.parseQuery(word);
        List<Posting> posts = c.getPostings(diskPosIndex);

        if (posts != null) {
            for (Posting x : posts)
                System.out.println("Doc ID: " + x.getDocumentId() + " " + corpus.getDocument(x.getDocumentId()).getTitle() + " " + x.getPositions());

            System.out.println("posting size: " + posts.size());
            if (posts.size() > 0) {
                System.out.print("Enter a Doc ID to view file's content \t\t(NUMBER ONLY!!!!!): ");
                Reader fileContent = corpus.getDocument(Integer.parseInt(keyboard.nextLine())).getContent();
                try {
                    EnglishTokenStream ets = new EnglishTokenStream(fileContent);
                    System.out.println();
                    for (String x : ets.getTokens())
                        System.out.print(x + " ");
                    ets.close();
                } catch (IOException e) {
                    System.out.println(e.getStackTrace());
                }
                System.out.println();
            }
        }
        if (posts == null || posts.size() < 3) {
            spellingCorrection(diskPosIndex, word);
        }
    }


    /**
     *
     * @param diskPosIndex
     * @param docWeightBytes
     * @param term
     * @param corpus
     */
    private static void rankedRetrieval(DiskPositionalIndex diskPosIndex, byte[] docWeightBytes, String term, DocumentCorpus corpus) {
        boolean needSpellCheck = false;
        String[] terms = term.split(" ");
        HashMap<Integer, Double> docIdsAds = new HashMap<>();
        for (int i = 0; i < terms.length; i++) {
            String[] stems = null;
            if (terms[i].contains("*")) {
                String[] matches = diskPosIndex.getWildcardMatches(terms[i]);
                stems = diskPosIndex.getProcessor().getStems(matches);
            } else {
                stems = diskPosIndex.getProcessor().processTokens(terms[i]);
            }
            for (int j = 0; j < stems.length; j++) {
                //System.out.println(stems[j]);
                int[][] docIdsTermFreq = diskPosIndex.getPostingsNoPositions(stems[j]);

                if (docIdsTermFreq == null) {
                    needSpellCheck = true;
                    break;
                }
                for (int k = 0; k < docIdsTermFreq.length; k++) {
                    double wqtMinusOne = ((double) (corpus.getCorpusSize())) / (double) docIdsTermFreq.length;
                    double wqt = Math.log(1 + wqtMinusOne);
                    double wdt = (1 + Math.log(docIdsTermFreq[k][1]));
                    //System.out.println("wqt "+Math.log(1+wqtMinusOne)+" for "+corpus.getDocument(docIdsTermFreq[k][0]).getTitle()+", term="+stems[j]);
                    //System.out.println("wdt "+(1+Math.log(docIdsTermFreq[k][1]))+" for "+corpus.getDocument(docIdsTermFreq[k][0]).getTitle()+", term="+stems[j]);
                    double adTemp = wdt * wqt;
                    if (!docIdsAds.isEmpty() && docIdsAds.containsKey(docIdsTermFreq[k][0])) {
                        docIdsAds.replace(docIdsTermFreq[k][0], docIdsAds.get(docIdsTermFreq[k][0]), docIdsAds.get(docIdsTermFreq[k][0]) + adTemp);
                    } else {
                        docIdsAds.put(docIdsTermFreq[k][0], adTemp);
                    }
                }
            }
            if (needSpellCheck) {
                break;
            }
        }

        if (needSpellCheck || docIdsAds.size() < 3) {
            spellingCorrection(diskPosIndex, term);
        } else {
            printOutTopK(docIdsAds,docWeightBytes,corpus);
        }
    }

    /**
     *
     * @param docIdsAds
     * @param docWeightBytes
     * @param corpus
     */
    private static void printOutTopK(HashMap<Integer,Double> docIdsAds, byte[] docWeightBytes, DocumentCorpus corpus)
    {
        for (Integer docId : docIdsAds.keySet()) {
            if (docIdsAds.get(docId) != 0) {
                int startPos = docId * 8;
                byte[] tempDocWeightBytes = new byte[8];
                for (int i = 0; i < tempDocWeightBytes.length; i++) {
                    tempDocWeightBytes[i] = docWeightBytes[startPos + i];
                }
                double docWeight = DiskPositionalIndex.bytesToDouble(tempDocWeightBytes);
                //System.out.println("Reading docWeight(Ld)"+docWeight+" for "+corpus.getDocument(docId).getTitle());
                docIdsAds.replace(docId, docIdsAds.get(docId), docIdsAds.get(docId) / docWeight);
            }
        }

        List<Map.Entry<Integer, Double>> list = new LinkedList<>(docIdsAds.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> e1, Map.Entry<Integer, Double> e2) {
                return (e2.getValue()).compareTo(e1.getValue());
            }
        });

        Map<Integer, Double> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        int i = 0;
        for (Integer docId : result.keySet()) {
            System.out.println("Accum:" + result.get(docId) + "\tDoc ID: " + docId + "\t" + corpus.getDocument(docId).getTitle());
            if (i == 9) {
                break;
            }
            i++;
        }
    }



    /**
     * MAIN METHOD FOR MILESTONE 2
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        //testing();
        Scanner keyboard = new Scanner(System.in);
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path corpusFolder = Paths.get(currentPath.toString(), "corpora");
        String dir = getDirectoryName(keyboard, corpusFolder);
        TokenProcessor tokenProcessor = new Milestone1TokenProcessor();

        boolean changeDirectory = false;

        while (!dir.equals("quit")) {
            currentPath = Paths.get(corpusFolder.toString(), dir);
            DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(currentPath, ".txt");
            ((DirectoryCorpus) corpus).registerFileDocumentFactory(".json", JsonFileDocument::loadJsonFileDocument);

            String queryBuild = "";

            if (!changeDirectory) {
                System.out.print("Enter \"query\" or \"build\" (or \"quit\" to exit): ");
                queryBuild = keyboard.nextLine();
            }

            while ((!queryBuild.equals("quit") && !queryBuild.equals(":q"))) {
                if (queryBuild.equals("build")) {
                    buildIndex(currentPath, tokenProcessor, corpus);
                } else if (queryBuild.equals("query") || changeDirectory) {
                    if (changeDirectory) {
                        changeDirectory = false;
                    }

                    KGramIndex kgi = readKGramFromDisk(currentPath);
                    DiskPositionalIndex diskPosIndex = new DiskPositionalIndex(currentPath, tokenProcessor, kgi);

                    System.out.print("Enter \"boolean\" or \"ranked\" (or \"quit\" to exit): ");
                    String queryType = keyboard.nextLine();

                    while (!queryType.equals("quit") && !queryType.equals(":q")) {
                        byte[] docWeightBytes = null;
                        if (queryType.equals("ranked")) {
                            docWeightBytes = readDocWeights(currentPath);
                        }
                        System.out.print("Enter term to search (or \"quit\" to exit): ");
                        String word = keyboard.nextLine();

                        while (!word.equals("quit") && !word.equals(":q")) {
                            String[] words = word.split(" ");
                            if (words[0].equals(":index")) {
                                if (hasDirectory(corpusFolder, words[1])) {
                                    word = "quit";
                                    dir = words[1];
                                    changeDirectory = true;
                                    queryBuild = "quit";
                                    queryType = "quit";
                                } else {
                                    System.out.println("Please recheck spelling of directory");
                                }
                            } else {
                                if (words[0].equals(":stem")) {
                                    String[] stems = (diskPosIndex.getProcessor().processTokens(words[1]));
                                    for (int i = 0; i < stems.length; i++) {
                                        System.out.println(i + ":" + stems[i]);
                                    }
                                } else if (words[0].equals(":vocab")) {
                                    for (int i = 0; i < 1000; i++) {
                                        System.out.println(diskPosIndex.getVocabulary().get(i));
                                    }
                                } else {
                                    if (queryType.equals("boolean")) {
                                        booleanQueryIndex(diskPosIndex, word, corpus, keyboard);
                                    } else if (queryType.equals("ranked")) {
                                        rankedRetrieval(diskPosIndex, docWeightBytes, word, corpus);
                                    }
                                }
                            }

                            if (!changeDirectory) {
                                System.out.print("Enter term to search (or \"quit\" to exit): ");
                                word = keyboard.nextLine();
                            }
                        }
                        if (word.equals(":q")) {
                            queryType = ":q";
                            queryBuild = ":q";
                        } else {
                            System.out.print("Enter \"boolean\" or \"ranked\" (or \"quit\" to exit): ");
                            queryType = keyboard.nextLine();
                        }
                    }
                    if (!queryBuild.equals("quit") && !queryBuild.equals(":q")) {
                        System.out.print("Enter \"query\" or \"build\" (or \"quit\" to exit): ");
                        queryBuild = keyboard.nextLine();
                    }
                }
                if (queryBuild.equals(":q")) {
                    dir = "quit";
                }
                if (queryBuild.equals("build") || queryBuild.equals("query")) {
                    System.out.print("Enter \"query\" or \"build\" (or \"quit\" to exit): ");
                    queryBuild = keyboard.nextLine();
                }
            }
            if (queryBuild.equals("quit") && !changeDirectory) {
                System.out.println();
                dir = getDirectoryName(keyboard, corpusFolder);
            }
        }
        keyboard.close();
    }

    /**
     * Prints the names of the directories given the path of the corpus directory
     *
     * @param corpusFolder Path - path of the folder that contains all possible corpora to index
     */
    private static void printDirectoryList(Path corpusFolder) {
        System.out.println("Directories");
        System.out.println("------------------------------");
        File[] files = new File(corpusFolder.toString()).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                System.out.println(files[i].getName());
            }
        }
    }

    /**
     * Prompts the user for the name of the corpus to index
     *
     * @param keyboard     Scanner - used to get user input
     * @param corpusFolder Path - path of the folder that contains all possible corpora to index
     * @return String "quit" or the String name of the directory
     */
    private static String getDirectoryName(Scanner keyboard, Path corpusFolder) {
        printDirectoryList(corpusFolder);
        System.out.print("\nEnter the name of the corpus to use (or \"quit\" to exit): ");
        String dir = keyboard.nextLine();

        while (!dir.equals("quit") && !dir.equals(":q")) {
            if (hasDirectory(corpusFolder, dir)) {
                return dir;
            } else {
                System.out.println("Please recheck spelling of directory");
            }
            System.out.print("Enter the name of the corpus to use (or \"quit\" to exit): ");
            dir = keyboard.nextLine();
        }
        return "quit";
    }

    /**
     * Checks if the parameter value String dir is a valid directory name
     *
     * @param corpusFolder Path - path of the folder that contains all possible corpora to index
     * @param dir          String - the directory name
     * @return boolean true if dir is valid false if not valid
     */
    private static boolean hasDirectory(Path corpusFolder, String dir) {
        File[] files = new File(corpusFolder.toString()).listFiles();

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                if (files[i].getName().equals(dir)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param currentPath
     * @return
     */
    private static KGramIndex readKGramFromDisk(Path currentPath) {
        //reading k-grams into k-gram object
        KGramIndex kgi = null;
        long startTime2 = System.currentTimeMillis();
        try {
            FileInputStream kGramsFile = new FileInputStream(currentPath.toString() + "/index/kgrams.bin");
            ObjectInputStream kGramsOIS = new ObjectInputStream(kGramsFile);

            kgi = (KGramIndex) kGramsOIS.readObject();

            kGramsOIS.close();
            kGramsFile.close();
        } catch (IOException ex) {
            System.out.println("IOException is caught");
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException is caught");
        }
        long endTime2 = System.currentTimeMillis();
        int numSeconds2 = ((int) ((endTime2 - startTime2) / 1000));
        System.out.println("Reading k-grams took " + numSeconds2 + " seconds");
        return kgi;
    }

    /**
     *
     * @param currentPath
     * @param tokenProcessor
     * @param corpus
     * @throws IOException
     */
    private static void buildIndex(Path currentPath, TokenProcessor tokenProcessor, DocumentCorpus corpus) throws IOException {
        //index the corpus - into PositionalInvertedIndex, writes docWeights.bin
        long startTime = System.currentTimeMillis();
        DiskIndexWriter indexWriter = new DiskIndexWriter(currentPath);
        PositionalInvertedIndex index = indexCorpus(tokenProcessor, corpus, indexWriter, currentPath);
        long endTime = System.currentTimeMillis();
        int numSeconds = ((int) ((endTime - startTime) / 1000));
        System.out.println("Indexing took " + numSeconds + " seconds");
        //building the corpus (writing to disk)
        long startTime1 = System.currentTimeMillis();
        indexWriter.writeIndex(index);
        indexWriter.closeVocabPostOutput();
        long endTime1 = System.currentTimeMillis();
        int numSeconds1 = ((int) ((endTime1 - startTime1) / 1000));
        System.out.println("Building took " + numSeconds1 + " seconds");
        //k-grams written to disk
        long startTime3 = System.currentTimeMillis();
        try {
            FileOutputStream kGramsFile = new FileOutputStream(currentPath.toString() + "/index/kgrams.bin");
            ObjectOutputStream kGramsOOS = new ObjectOutputStream(kGramsFile);
            kGramsOOS.writeObject(index.getKGramIndex());
            kGramsOOS.close();
            kGramsFile.close();
        } catch (IOException e) {
            System.out.println("Kgrams ioexeception");
        }
        long endTime3 = System.currentTimeMillis();
        int numSeconds3 = ((int) ((endTime3 - startTime3) / 1000));
        System.out.println("Writing k-grams to disk took " + numSeconds3 + " seconds");
    }

    /**
     *
     * @param currentPath
     * @return
     */
    private static byte[] readDocWeights(Path currentPath) {
        try {
            File docWeights = new File(currentPath.toString() + "/index/docWeights.bin");
            InputStream readDocWeights = new FileInputStream(docWeights);
            DataInputStream dis = new DataInputStream(readDocWeights);
            byte[] docWeightBytes = new byte[(int) docWeights.length()];
            dis.read(docWeightBytes);
            return docWeightBytes;
        } catch (IOException e) {
            System.out.println("IOException in readDOcWeights");
        }
        return null;
    }

    /**
     * Indexes the selected corpus
     *
     * @param corpus      - the corpus to index
     * @param indexWriter - the disk writer is used to calculate docWeights in this method
     * @return Index contains info on every term in every document within selected corpus
     * @throws IOException
     */
    private static PositionalInvertedIndex indexCorpus(TokenProcessor processor, DocumentCorpus corpus, DiskIndexWriter indexWriter, Path corpusFolder) throws IOException {
        HashSet<String> KGI = new HashSet<>();
        Iterable<Document> itrDoc = corpus.getDocuments();
        PositionalInvertedIndex posInvertedIndex = new PositionalInvertedIndex(processor);

        int positionCounter;
        int numWritten = 0;

        for (Document doc : itrDoc) {
            positionCounter = 0;
            Reader readDoc = doc.getContent();
            EnglishTokenStream ets = new EnglishTokenStream(readDoc);
            Iterable<String> engTokens = ets.getTokens();
            //key=term, value=num of times the term appears in the doc, term freq
            HashMap<String, Integer> typeTermFreq = new HashMap<>();
            for (String engTok : engTokens) {
                String[] types = posInvertedIndex.getProcessor().processButDontStemTokensAKAGetType(engTok);
                //adding TYPES
                for (String x : types)
                    KGI.add(x);

                String[] stems = posInvertedIndex.getProcessor().getStems(types);
                for (int i = 0; i < stems.length; i++) {
                    if (stems.length > 1) {
                        if (i == 0) {
                            positionCounter++;
                        }
                        posInvertedIndex.addTerm(stems[i], doc.getId(), positionCounter);
                    } else {
                        positionCounter++;
                        posInvertedIndex.addTerm(stems[i], doc.getId(), positionCounter);
                    }

                    if (!typeTermFreq.isEmpty() && typeTermFreq.containsKey(stems[i])) {
                        typeTermFreq.replace(stems[i], typeTermFreq.get(stems[i]), typeTermFreq.get(stems[i]) + 1);
                    } else {
                        typeTermFreq.put(stems[i], 1);
                    }
                }
            }
            double sumWDT = 0;
            for (String type : typeTermFreq.keySet()) {
                double wdt = (1 + Math.log((double) typeTermFreq.get(type)));
                wdt *= wdt;
                sumWDT += wdt;
            }
            double ld = Math.sqrt(sumWDT);
            indexWriter.writeDocWeight(ld);
            ets.close();
            numWritten++;
        }
        indexWriter.closeDocWeights();
        posInvertedIndex.addToKGI(KGI);
        return posInvertedIndex;
    }

    /**
     *
     */
    private static void testing() {
                /*double x = 5;
        int y = 2;
        System.out.println(x/y);*/
        /*String term = "0wowicantbelieveit";
        String term1 = "0cannibalistically";

        System.out.println(getEditDistance(term,term1));*/

        /*try
        {
            Path currentPath = Paths.get(System.getProperty("user.dir"));

            Path corpusFolder = Paths.get(currentPath.toString(), "corpora/vocab.bin");
            File vocabFile = new File(corpusFolder.toString());
            InputStream vocabIS = new FileInputStream(vocabFile);
            DataInputStream vocabDIS = new DataInputStream(vocabIS);
            byte[] vocabFileBytes = new byte[(int)vocabFile.length()];
            vocabDIS.read(vocabFileBytes);

            corpusFolder = Paths.get(currentPath.toString(), "corpora/vocabTable.bin");
            File vocabTableFile = new File(corpusFolder.toString());
            InputStream vocabTableIS = new FileInputStream(vocabTableFile);
            DataInputStream vocabTableDIS = new DataInputStream(vocabTableIS);
            byte[] vocabTableBytes = new byte[(int)vocabTableFile.length()];
            vocabTableDIS.read(vocabTableBytes);

            File postingsFile = new File(corpusFolder.toString()+"corpora/vocabTable.bin");
            InputStream postingsIS = new FileInputStream(postingsFile);
            DataInputStream postingsDIS = new DataInputStream(postingsIS);
            postingsBytes = new byte[(int)postingsFile.length()];
            postingsDIS.read(postingsBytes);

            int lowVTAIndex = 0;
            int highVTAIndex = (vocabTableBytes.length / 16) - 1;
            int maxVTAIndex = highVTAIndex;
            int midVTAIndex = -1;
            int midVTByteIndex = -1;

            String term = "catcher";


            while(lowVTAIndex <= highVTAIndex)
            {
                String word = "";
                midVTAIndex = (lowVTAIndex + highVTAIndex)/2;
                midVTByteIndex = midVTAIndex * 16;

                byte[] midLongBytes = new byte[8];
                for (int i = 0; i < midLongBytes.length; i++) {
                    midLongBytes[i] = vocabTableBytes[midVTByteIndex + i];
                }
                long midVocabStartIndex = DiskPositionalIndex.bytesToLong(midLongBytes);
                System.out.println("midVocabStartIndex="+midVocabStartIndex);

                midVTAIndex++;
                midVTByteIndex = midVTAIndex * 16;

                System.out.println("lowVTAIndex="+lowVTAIndex);
                System.out.println("highVTAIndex="+highVTAIndex);

                if(lowVTAIndex == highVTAIndex && lowVTAIndex == maxVTAIndex)
                {
                    byte[] vocabBytes = new byte[vocabFileBytes.length - (int) midVocabStartIndex];
                    for (int i = 0; i < vocabBytes.length; i++) {
                        vocabBytes[i] = vocabFileBytes[(int) midVocabStartIndex + i];
                    }
                    word = new String(vocabBytes);
                }
                else {
                    byte[] midPLUS1LongBytes = new byte[8];
                    for (int i = 0; i < midPLUS1LongBytes.length; i++) {
                        midPLUS1LongBytes[i] = vocabTableBytes[midVTByteIndex + i];
                    }
                    long midPLUS1VocabStartIndex = DiskPositionalIndex.bytesToLong(midPLUS1LongBytes);

                    long vocabLength = midPLUS1VocabStartIndex - midVocabStartIndex;

                    byte[] vocabBytes = new byte[(int) vocabLength];
                    for (int i = 0; i < vocabBytes.length; i++) {
                        vocabBytes[i] = vocabFileBytes[(int) midVocabStartIndex + i];
                    }

                    word = new String(vocabBytes);
                }
                System.out.println("word=" + word);

                if (term.compareTo(word) > 0) {
                    System.out.println("term is in the last half");
                    //term is in the last half
                    //highVTAIndex = 0;
                    lowVTAIndex = midVTAIndex;

                } else if (term.compareTo(word) < 0) {
                    System.out.println("term is in the first half");
                    //term is in the first half
                    highVTAIndex = midVTAIndex - 2;
                } else {
                    System.out.println("Term was found at index " + (midVTAIndex - 1));
                    break;
                }
            }
            byte[] postingPos = new byte[8];
            for (int i = 0; i < postingPos.length; i++) {
                postingPos[i] = vocabTableBytes[(midVTAIndex-1)*16 + i + 8];
            }
            System.out.println(DiskPositionalIndex.bytesToLong(postingPos));

            byte[] numOfDocsBytes = new byte[4];
            for (int i = 0; i < numOfDocsBytes.length; i++) {
                numOfDocsBytes[i] = postingsBytes[(int)postingPos + i];
            }
            int numOfDocs = bytesToInt(numOfDocsBytes);
        }
        catch(IOException e){}*/
        /*try
        {
            Path currentPath = Paths.get(System.getProperty("user.dir"));
            Path corpusFolder = Paths.get(currentPath.toString(), "corpora");
            FileOutputStream vocabFile = new FileOutputStream(corpusFolder.toString()+"/vocab.bin");
            DataOutputStream vocabData = new DataOutputStream(vocabFile);

            FileOutputStream vocabTableFile = new FileOutputStream(corpusFolder.toString()+"/vocabTable.bin");
            DataOutputStream vocabTableData = new DataOutputStream(vocabTableFile);

            FileOutputStream postingsFile = new FileOutputStream(corpusFolder.toString()+"/postings.bin");
            DataOutputStream postingsData = new DataOutputStream(postingsFile);

            String term = "aangelsbaseballcatcherdodgers";
            byte[] termBytes = term.getBytes();
            vocabData.write(termBytes);

//0            //a
            vocabTableData.writeLong(0);
            vocabTableData.writeLong(0);

            postingsData.writeInt(1); //dft
            postingsData.writeInt(1); //id is actually 1
            postingsData.writeInt(1); //tftd
            postingsData.writeInt(1); //p

//1            //angels
            vocabTableData.writeLong(1);
            vocabTableData.writeLong(16);

            postingsData.writeInt(1); //dft
            postingsData.writeInt(20); //id is actually 20
            postingsData.writeInt(2); //tftd
            postingsData.writeInt(1); //p
            postingsData.writeInt(10); //p

//2            //baseball
            vocabTableData.writeLong(7);
            vocabTableData.writeLong(36);

            postingsData.writeInt(2); //dft
            postingsData.writeInt(30); //id is actually 30
            postingsData.writeInt(1); //tftd
            postingsData.writeInt(1); //p

            postingsData.writeInt(3); //id is actually 33
            postingsData.writeInt(1); //tftd
            postingsData.writeInt(1); //p

//3            //catcher
            vocabTableData.writeLong(15);
            vocabTableData.writeLong(64);

            postingsData.writeInt(1); //dft
            postingsData.writeInt(4); //id is actually 4
            postingsData.writeInt(2); //tftd
            postingsData.writeInt(1); //p
            postingsData.writeInt(10); //p

//4            //dodgers
            vocabTableData.writeLong(22);
            vocabTableData.writeLong(84);

            postingsData.writeInt(1); //dft
            postingsData.writeInt(5); //id is actually 5
            postingsData.writeInt(1); //tftd
            postingsData.writeInt(1); //p

            postingsData.close();
            vocabData.close();
            vocabTableData.close();
        }
        catch (IOException e) {System.out.println("IO exception"); e.printStackTrace(); }*/
    }
}