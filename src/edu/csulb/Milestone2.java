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

    private static void queryIndex(String word, Index index, DocumentCorpus corpus, Scanner keyboard) {
        BooleanQueryParser pa = new BooleanQueryParser();
        QueryComponent c = pa.parseQuery(word);
        List<Posting> posts = c.getPostings(index);

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

    private static long bytesToLong(byte[] bytes)
    {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    /**
     * MAIN METHOD FOR MILESTONE 2
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        try
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

            int lowVTAIndex = 0;
            int highVTAIndex = (vocabTableBytes.length / 16) - 1;
            int midVTAIndex;
            int midVTByteIndex;

            String term = "a";

            while(lowVTAIndex <= highVTAIndex)
            {
                midVTAIndex = (lowVTAIndex + highVTAIndex)/2;
                midVTByteIndex = midVTAIndex * 16;

                byte[] midLongBytes = new byte[8];
                for (int i = 0; i < midLongBytes.length; i++) {
                    midLongBytes[i] = vocabTableBytes[midVTByteIndex + i];
                }
                long midVocabStartIndex = bytesToLong(midLongBytes);

                midVTAIndex++;
                midVTByteIndex = midVTAIndex * 16;

                byte[] midPLUS1LongBytes = new byte[8];
                for (int i = 0; i < midPLUS1LongBytes.length; i++) {
                    midPLUS1LongBytes[i] = vocabTableBytes[midVTByteIndex + i];
                }
                long midPLUS1VocabStartIndex = bytesToLong(midPLUS1LongBytes);

                long vocabLength = midPLUS1VocabStartIndex - midVocabStartIndex;

                byte[] vocabBytes = new byte[(int) vocabLength];
                for (int i = 0; i < vocabBytes.length; i++) {
                    vocabBytes[i] = vocabFileBytes[(int)midVocabStartIndex + i];
                }

                String word = new String(vocabBytes);
                System.out.println("word="+word);

                if (term.compareTo(word) > 0) {
                    System.out.println("term is in the last half");
                    //term is in the last half
                    lowVTAIndex = midVTAIndex;
                } else if (term.compareTo(word) < 0) {
                    System.out.println("term is in the first half");
                    //term is in the first half
                    highVTAIndex = midVTAIndex - 2;
                } else {
                    System.out.println("Term was found at index "+(midVTAIndex-1));
                    break;
                }
            }




            /*while (true) {
                try {
                    System.out.println(vocabTableDIS.readLong());
                } catch (EOFException e) {
                    break;
                }
            }*/
        }
        catch(IOException e){}
        /*try
        {
            Path currentPath = Paths.get(System.getProperty("user.dir"));
            Path corpusFolder = Paths.get(currentPath.toString(), "corpora");
            FileOutputStream vocabFile = new FileOutputStream(corpusFolder.toString()+"/vocab.bin");
            DataOutputStream vocabData = new DataOutputStream(vocabFile);

            FileOutputStream vocabTableFile = new FileOutputStream(corpusFolder.toString()+"/vocabTable.bin");
            DataOutputStream vocabTableData = new DataOutputStream(vocabTableFile);

            String term = "aangelsbaseballcatcherdodgers";
            byte[] termBytes = term.getBytes();
            vocabData.write(termBytes);

//0            //a
            vocabTableData.writeLong(0);
            vocabTableData.writeLong(0);
//1            //angels
            vocabTableData.writeLong(1);
            vocabTableData.writeLong(20);
//2            //baseball
            vocabTableData.writeLong(7);
            vocabTableData.writeLong(100);
//3            //catcher
            vocabTableData.writeLong(15);
            vocabTableData.writeLong(140);
//4            //dodgers
            vocabTableData.writeLong(22);
            vocabTableData.writeLong(400);

            vocabData.close();
            vocabTableData.close();
        }
        catch (IOException e) {System.out.println("IO exception"); e.printStackTrace(); }
        */
        /*Scanner keyboard = new Scanner(System.in);
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path corpusFolder = Paths.get(currentPath.toString(), "corpora");

        String dir = getDirectoryName(keyboard, corpusFolder);
        boolean changeDirectory = false;

        while (!dir.equals("quit"))
        {
            currentPath = Paths.get(corpusFolder.toString(), dir);
            DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(currentPath, ".txt");
            ((DirectoryCorpus) corpus).registerFileDocumentFactory(".json", JsonFileDocument::loadJsonFileDocument);

            //index the corpus
            long startTime = System.currentTimeMillis();
            DiskIndexWriter indexWriter = new DiskIndexWriter(currentPath);
            Index index = indexCorpus(corpus, indexWriter);
            long endTime = System.currentTimeMillis();
            int numSeconds = ((int) ((endTime - startTime) / 1000));
            System.out.println("Indexing took " + numSeconds + " seconds");
            String queryBuild = "";

            if (!changeDirectory) {
                System.out.print("Enter \"query\" or \"build\" (or \"quit\" to exit): ");
                queryBuild = keyboard.nextLine();
            }

            while ((!queryBuild.equals("quit") && !queryBuild.equals(":q"))) {
                if (queryBuild.equals("build"))
                {
                    //WRITE TO DISK
                    indexWriter.writeIndex(index);
                }
                else if (queryBuild.equals("query") || changeDirectory)
                {
                    if (changeDirectory)
                    {
                        changeDirectory = false;
                    }
                    System.out.print("Enter term to search (or \"quit\" to exit): ");
                    String word = keyboard.nextLine();

                    while(!word.equals("quit") && !word.equals(":q"))
                    {
                        String[] words = word.split(" ");
                        if (words[0].equals(":index")) {
                            if (hasDirectory(corpusFolder, words[1])) {
                                word = "quit";
                                dir = words[1];
                                changeDirectory = true;
                                queryBuild = "quit";
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
                                queryIndex(word, index, corpus, keyboard);
                            }
                        }

                        if(!changeDirectory) {
                            System.out.print("Enter term to search (or \"quit\" to exit): ");
                            word = keyboard.nextLine();
                        }
                    }
                    if (word.equals(":q"))
                    {
                        queryBuild = ":q";
                    }
                }
                if(!queryBuild.equals("quit") && !queryBuild.equals(":q"))
                {
                    System.out.print("Enter \"query\" or \"build\" (or \"quit\" to exit): ");
                    queryBuild = keyboard.nextLine();
                }
            }
            if (queryBuild.equals(":q"))
            {
                dir = "quit";
            }
            else if (queryBuild.equals("quit") && !changeDirectory)
            {
                System.out.println();
                dir = getDirectoryName(keyboard, corpusFolder);
            }
        }
        keyboard.close();*/
    }

    /**
     * Indexes the selected corpus
     * @param corpus - the corpus to index
     * @param indexWriter - the disk writer is used to calculate docWeights in this method
     * @return Index contains info on every term in every document within selected corpus
     * @throws IOException
     */
    private static Index indexCorpus(DocumentCorpus corpus, DiskIndexWriter indexWriter) throws IOException {
        HashSet<String> KGI = new HashSet<>();
        Iterable<Document> itrDoc = corpus.getDocuments();
        TokenProcessor processor = new Milestone1TokenProcessor();
        PositionalInvertedIndex posInvertIndex = new PositionalInvertedIndex(processor);
        int positionCounter;
        int numDocs = 0;

        for (Document doc : itrDoc) {
            numDocs++;
            positionCounter = 0;
            Reader readDoc = doc.getContent();
            EnglishTokenStream ets = new EnglishTokenStream(readDoc);
            Iterable<String> engTokens = ets.getTokens();
            //term, num of times the term appears in the doc
            HashMap<String, Integer> docVocab = new HashMap<>();
            for (String engTok : engTokens) {
                String[] types = posInvertIndex.getProcessor().processButDontStemTokensAKAGetType(engTok);
                //adding TYPES
                for(String x: types)
                    KGI.add(x);

                String[] stems = posInvertIndex.getProcessor().getStems(types);
                for (int i = 0; i < stems.length; i++) {
                    if (stems.length > 1) {
                        if (i == 0) {
                            positionCounter++;
                        }
                        posInvertIndex.addTerm(stems[i], doc.getId(), positionCounter);
                    } else {
                        positionCounter++;
                        posInvertIndex.addTerm(stems[i], doc.getId(), positionCounter);
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
        // add the entire hashset to the KGI
        //System.out.println("vocab:"+posInvertIndex.getVocabulary().size());
        //System.out.println("KGI size:"+KGI.size());
        posInvertIndex.addToKGI(KGI);
        return posInvertIndex;
    }
}