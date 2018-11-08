package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.index.*;
import cecs429.queryparser.BooleanQueryParser;
import cecs429.queryparser.QueryComponent;
import cecs429.text.EnglishTokenStream;
import cecs429.text.Milestone1TokenProcessor;
import cecs429.text.TokenProcessor;
import com.sun.tools.javac.util.ArrayUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Contains the main for Milestone 2
 */
public class Milestone2 {
    /**
     * Prints the names of the directories given the path of the corpus directory
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
     * @param keyboard Scanner - used to get user input
     * @param corpusFolder Path - path of the folder that contains all possible corpora to index
     * @return String "quit" or the String name of the directory
     */
    private static String getDirectoryName(Scanner keyboard, Path corpusFolder) {
        printDirectoryList(corpusFolder);
        System.out.print("\nEnter the name of the corpus to index (or \"quit\" to exit): ");
        String dir = keyboard.nextLine();

        while (!dir.equals("quit") && !dir.equals(":q")) {
            if (hasDirectory(corpusFolder, dir)) {
                return dir;
            } else {
                System.out.println("Please recheck spelling of directory");
            }
            System.out.print("Enter the name of the corpus to index (or \"quit\" to exit): ");
            dir = keyboard.nextLine();
        }
        return "quit";
    }

    /**
     * Checks if the parameter value String dir is a valid directory name
     * @param corpusFolder Path - path of the folder that contains all possible corpora to index
     * @param dir String - the directory name
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

    private static void booleanQueryIndex(Path currentPath, String word, DocumentCorpus corpus, Scanner keyboard, TokenProcessor processor) {
        DiskPositionalIndex diskPosIndex = new DiskPositionalIndex(currentPath, processor);
        BooleanQueryParser pa = new BooleanQueryParser();
        QueryComponent c = pa.parseQuery(word);
        List<Posting> posts = c.getPostings(diskPosIndex);

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
            }
            catch(IOException e) {System.out.println(e.getStackTrace());}
            System.out.println();
        }
    }

    public static byte[] readDocWeights(Path currentPath)
    {
        try {
            File docWeights = new File(currentPath.toString() + "/index/docWeights.bin");
            InputStream readDocWeights = new FileInputStream(docWeights);
            DataInputStream dis = new DataInputStream(readDocWeights);
            byte[] docWeightBytes = new byte[(int)docWeights.length()];
            dis.read(docWeightBytes);
            return docWeightBytes;
        }
        catch (IOException e){System.out.println("IOException in readDOcWeights");}
        return null;
    }


    private static void rankedRetrieval(byte[] docWeightBytes, Path currentPath, String term, DocumentCorpus corpus, TokenProcessor processor)
    {
        DiskPositionalIndex diskPosIndex = new DiskPositionalIndex(currentPath, processor);

        try {
            String[] terms = term.split(" ");
            HashMap<Integer, Double> docIdsAds = new HashMap<>();
            for (int k = 0; k < terms.length; k++)
            {
                String[] stems = diskPosIndex.getProcessor().processTokens(terms[k]);

                for (int i = 0; i < stems.length; i++)
                {
                    int[][] docIdsTermFreq = diskPosIndex.getPostingsNoPositions(stems[i]);
                    double wqt = corpus.getCorpusSize();
                    wqt /= docIdsTermFreq.length;
                    wqt++;
                    wqt = Math.log(wqt);


                    for (int j = 0; j < docIdsTermFreq.length; j++)
                    {
                        double wdt = Math.log(docIdsTermFreq[j][1]);
                        wdt++;
                        double ad = wdt * wqt;

                        if (!docIdsAds.containsKey(docIdsTermFreq[j][0]))
                        {
                            docIdsAds.put(docIdsTermFreq[j][0],ad);
                        }
                        else
                        {
                            docIdsAds.replace(docIdsTermFreq[j][0],docIdsAds.get(docIdsTermFreq[j][0]),docIdsAds.get(docIdsTermFreq[j][0])+ad);
                        }
                    }
                }

            }

            for (Integer docId : docIdsAds.keySet())
            {
                if (docIdsAds.get(docId) != 0)
                {
                    int startPos = docId*8;
                    byte[] tempDocWeightBytes = new byte[8];
                    for (int i = 0; i < tempDocWeightBytes.length; i++) {
                        tempDocWeightBytes[i] = docWeightBytes[startPos + i];
                    }
                    double docWeight = DiskPositionalIndex.bytesToDouble(tempDocWeightBytes);
                    docIdsAds.replace(docId,docIdsAds.get(docId),docIdsAds.get(docId)/docWeight);
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
            int i = 0;
            for (Map.Entry<Integer, Double> entry : list) {
                result.put(entry.getKey(), entry.getValue());
                System.out.println("Accum:"+entry.getValue()+"    Doc ID: " + entry.getKey() + " " + corpus.getDocument(entry.getKey()).getTitle());
                if (i == 10)
                {
                    break;
                }
            }

        } catch (NullPointerException e) {
            System.out.println(term + " was not found");
        }
    }

    /**
     * MAIN METHOD FOR MILESTONE 2
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
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

        Scanner keyboard = new Scanner(System.in);
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path corpusFolder = Paths.get(currentPath.toString(), "corpora");

        String dir = getDirectoryName(keyboard, corpusFolder);
        boolean changeDirectory = false;

        while (!dir.equals("quit")) {
            currentPath = Paths.get(corpusFolder.toString(), dir);
            DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(currentPath, ".txt");
            ((DirectoryCorpus) corpus).registerFileDocumentFactory(".json", JsonFileDocument::loadJsonFileDocument);

            //index the corpus
            long startTime = System.currentTimeMillis();
            DiskIndexWriter indexWriter = new DiskIndexWriter(currentPath);
            PositionalInvertedIndex index = indexCorpus(corpus, indexWriter, currentPath);
            long endTime = System.currentTimeMillis();
            int numSeconds = ((int) ((endTime - startTime) / 1000));
            System.out.println("Indexing took " + numSeconds + " seconds");
            String queryBuild = "";

            if (!changeDirectory) {
                System.out.print("Enter \"query\" or \"build\" (or \"quit\" to exit): ");
                queryBuild = keyboard.nextLine();
            }

            while ((!queryBuild.equals("quit") && !queryBuild.equals(":q"))) {
                if (queryBuild.equals("build")) {
                    indexWriter.writeIndex(index);
                    indexWriter.closeVocabPostOutput();
                } else if (queryBuild.equals("query") || changeDirectory) {
                    if (changeDirectory) {
                        changeDirectory = false;
                    }

                    System.out.print("Enter \"boolean\" or \"ranked\" (or \"quit\" to exit): ");
                    String queryType = keyboard.nextLine();

                    while (!queryType.equals("quit") && !queryType.equals(":q")) {
                        byte[] docWeightBytes = null;
                        if (queryType.equals("ranked"))
                        {
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
                                    String[] stems = index.getProcessor().processTokens(words[1]);
                                    for (int i = 0; i < stems.length; i++) {
                                        System.out.println(i + ":" + stems[i]);
                                    }
                                } else if (words[0].equals(":vocab")) {
                                    for (int i = 0; i < 1000; i++) {
                                        System.out.println(index.getVocabulary().get(i));
                                    }
                                } else {
                                    if (queryType.equals("boolean")) {
                                        booleanQueryIndex(currentPath, word, corpus, keyboard, index.getProcessor());
                                    }
                                    else if (queryType.equals("ranked"))
                                    {
                                        rankedRetrieval(docWeightBytes,currentPath, word,corpus, index.getProcessor());
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
                        }
                        else {
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
                if (queryBuild.equals("build") || queryBuild.equals("query"))
                {
                    System.out.print("Enter \"query\" or \"build\" (or \"quit\" to exit): ");
                    queryBuild = keyboard.nextLine();
                }
            }
            if (queryBuild.equals("quit") && !changeDirectory)
            {
                System.out.println();
                dir = getDirectoryName(keyboard, corpusFolder);
            }
        }
        keyboard.close();
    }

    /**
     * Indexes the selected corpus
     * @param corpus - the corpus to index
     * @param indexWriter - the disk writer is used to calculate docWeights in this method
     * @return Index contains info on every term in every document within selected corpus
     * @throws IOException
     */
    private static PositionalInvertedIndex indexCorpus(DocumentCorpus corpus, DiskIndexWriter indexWriter, Path corpusFolder) throws IOException {
        HashSet<String> KGI = new HashSet<>();
        Iterable<Document> itrDoc = corpus.getDocuments();
        TokenProcessor processor = new Milestone1TokenProcessor();
        PositionalInvertedIndex posInvertedIndex = new PositionalInvertedIndex(processor);

        int positionCounter;

        for (Document doc : itrDoc) {
            positionCounter = 0;
            Reader readDoc = doc.getContent();
            EnglishTokenStream ets = new EnglishTokenStream(readDoc);
            Iterable<String> engTokens = ets.getTokens();
            //term, num of times the term appears in the doc
            HashMap<String, Integer> docVocab = new HashMap<>();
            for (String engTok : engTokens) {
                String[] types = posInvertedIndex.getProcessor().processButDontStemTokensAKAGetType(engTok);
                //adding TYPES
                for(String x: types)
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

                    if (!docVocab.isEmpty() && docVocab.containsKey(stems[i]))
                    {
                        docVocab.replace(stems[i],docVocab.get(stems[i]),docVocab.get(stems[i])+1);
                    }
                    else
                    {
                        docVocab.put(stems[i],1);
                    }
                }
            }
            double[] termWeightsSquared = new double[docVocab.size()];
            int i = 0;
            for (String term : docVocab.keySet())
            {
                termWeightsSquared[i] = 1 + Math.log(docVocab.get(term));
                termWeightsSquared[i] *= termWeightsSquared[i];
                i++;
            }
            double docWeight = 0;
            for (int j = 0; j < termWeightsSquared.length; j++)
            {
                docWeight += termWeightsSquared[j];
            }
            docWeight = Math.sqrt(docWeight);
            indexWriter.writeDocWeight(docWeight);
            ets.close();
        }
        indexWriter.closeDocWeights();
        posInvertedIndex.addToKGI(KGI);
        return posInvertedIndex;
    }
}