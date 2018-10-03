package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.queryparser.BooleanQueryParser;
import cecs429.queryparser.QueryComponent;
import cecs429.text.EnglishTokenStream;
import cecs429.text.Milestone1TokenProcessor;
import cecs429.text.TokenProcessor;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

/**
 *
 */
public class Milestone1UsingPosInvertIndex {
    /**
     * Prints the names of the directories given the path of the corpus directory
     *
     * @param corpusFolder
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
     * @param keyboard
     * @param corpusFolder
     * @return String the
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
     * @param corpusFolder
     * @param dir
     * @return
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
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Scanner keyboard = new Scanner(System.in);
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path corpusFolder = Paths.get(currentPath.toString(), "corpora");

        String dir = getDirectoryName(keyboard, corpusFolder);
        while (!dir.equals("quit") && !dir.equals(":q")) {
            boolean changeDirectory = false;
            currentPath = Paths.get(corpusFolder.toString(), dir);
            DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(currentPath, ".txt");
            ((DirectoryCorpus) corpus).registerFileDocumentFactory(".json", JsonFileDocument::loadJsonFileDocument);

            //index the corpus
            long startTime = System.currentTimeMillis();
            Index index = indexCorpus(corpus);
            long endTime = System.currentTimeMillis();
            int numSeconds = ((int) ((endTime - startTime) / 1000));
            System.out.println("Indexing took " + numSeconds + " seconds");

            System.out.print("Enter term to search (or \"quit\" to exit): ");
            String word = keyboard.nextLine();
            while (!word.equals("quit") && !word.equals(":q")) {
                String[] words = word.split(" ");
                if (words[0].equals(":index")) {
                    if (hasDirectory(corpusFolder, words[1])) {
                        word = "quit";
                        dir = words[1];
                        changeDirectory = true;
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
                        BooleanQueryParser pa = new BooleanQueryParser();
                        QueryComponent c = pa.parseQuery(word);
                        List<Posting> posts = c.getPostings(index);

                        for (Posting x : posts)
                            System.out.println("Doc ID: " + x.getDocumentId() + " " + corpus.getDocument(x.getDocumentId()).getTitle() + " " + x.getPositions());

                        System.out.println("posting size: " + posts.size());
                        if (posts.size() > 0) {
                            System.out.print("Enter a Doc ID to view file's content: ");
                            Reader fileContent = corpus.getDocument(Integer.parseInt(keyboard.nextLine())).getContent();
                            EnglishTokenStream ets = new EnglishTokenStream(fileContent);
                            System.out.println();
                            for (String x : ets.getTokens())
                                System.out.print(x + " ");
                            ets.close();
                            System.out.println();
                        }
                    }
                }
                System.out.print("\nEnter term to search (or \"quit\" to exit): ");
                word = keyboard.nextLine();
            }
            if (!word.equals(":q")) { //word must equal "quit" to go in here
                if (!changeDirectory) {
                    dir = getDirectoryName(keyboard, corpusFolder);
                }
            } else if (word.equals(":q"))// word == :q so quit program
            {
                dir = "quit";
            }
        }
        keyboard.close();
    }

    /**
     * @param corpus
     * @return
     * @throws IOException
     */
    private static Index indexCorpus(DocumentCorpus corpus) throws IOException {
        HashSet<String> KGI = new HashSet<>();
        Iterable<Document> itrDoc = corpus.getDocuments();
        TokenProcessor processor = new Milestone1TokenProcessor();
        PositionalInvertedIndex posInvertIndex = new PositionalInvertedIndex(processor);
        int positionCounter;

        for (Document doc : itrDoc) {
            positionCounter = 0;
            Reader readDoc = doc.getContent();
            EnglishTokenStream ets = new EnglishTokenStream(readDoc);
            Iterable<String> engTokens = ets.getTokens();
            for (String engTok : engTokens) {
                String[] types = posInvertIndex.getProcessor().processButDontStemTokensAKAGetType(engTok);
                for(String x: types)
                    KGI.add(x);

                //posInvertIndex.addToKGI(types);
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
                }
            }
            System.out.println("finished indexing doc #" + doc.getId());
            ets.close();
        }
        // add the entire hashset to the KGI
        posInvertIndex.addToKGI(KGI);
        return posInvertIndex;
    }
}