package cecs429.index;

import cecs429.text.TokenProcessor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.nio.file.Path;

/**
 *  Responsible for reading in the on disk files and translating them
 *  to their respective Java objects
 */
public class DiskPositionalIndex implements Index {
    /**
     *  Represents the on disk index
     */
    private byte[] vocabFileBytes, vocabTableBytes, postingsBytes;
    /**
     *  class values represented as bytes to be converted to higher level
     *  objects
     */
    private TokenProcessor processor;

    /**
     *  Processor for stemming tokens
     */
    private KGramIndex kgi;

    /**
     * @param corpusFolder Directory where all bin files to be read are located
     * @param processor Processor for processing tokens
     * @param kgi   the KGram Index
     */
    public DiskPositionalIndex(Path corpusFolder, TokenProcessor processor, KGramIndex kgi) {
        this.kgi = kgi;
        this.processor = processor;
        try {
            File vocabFile = new File(corpusFolder.toString() + "/index/vocab.bin");
            InputStream vocabIS = new FileInputStream(vocabFile);
            DataInputStream vocabDIS = new DataInputStream(vocabIS);
            vocabFileBytes = new byte[(int) vocabFile.length()];
            vocabDIS.read(vocabFileBytes);

            File vocabTableFile = new File(corpusFolder.toString() + "/index/vocabTable.bin");
            InputStream vocabTableIS = new FileInputStream(vocabTableFile);
            DataInputStream vocabTableDIS = new DataInputStream(vocabTableIS);
            vocabTableBytes = new byte[(int) vocabTableFile.length()];
            vocabTableDIS.read(vocabTableBytes);

            File postingsFile = new File(corpusFolder.toString() + "/index/postings.bin");
            InputStream postingsIS = new FileInputStream(postingsFile);
            DataInputStream postingsDIS = new DataInputStream(postingsIS);
            postingsBytes = new byte[(int) postingsFile.length()];
            postingsDIS.read(postingsBytes);
        } catch (IOException e) {
            System.out.println("IO exception");
            e.printStackTrace();
        }
    }

    /**
     * Responsible for returning a list of postings without
     * positions
     * @param term term to retrieve the postings without positions for
     * @return returns a 2d array with the first index representing the
     * docid and with second index representing doc frequency [0] and
     * [1] for term frequency
     */
    @Override
    public int[][] getPostingsNoPositions(String term) {
        //System.out.println("postingsBytes.length:"+postingsBytes.length);
        try {
            long postingPos = binarySearchVocabTable(term);
            //System.out.println("postingPos:"+postingPos);
            if (postingPos != -1) {
                byte[] numOfDocsBytes = new byte[4];
                for (int i = 0; i < numOfDocsBytes.length; i++) {
                    numOfDocsBytes[i] = postingsBytes[(int) postingPos + i];
                }
                int numOfDocs = bytesToInt(numOfDocsBytes);
                //System.out.println("numOfDocs=" + numOfDocs);

                int[][] docIdsTermFreqs = new int[numOfDocs][2];
                int nextIntBytePos = (int) postingPos + 4;

                for (int i = 0; i < numOfDocs; i++) {
                    byte[] docIdBytes = new byte[4];
                    //System.out.print("startDocIdBytes=" + nextIntBytePos);
                    for (int j = 0; j < docIdBytes.length; j++) {
                        docIdBytes[j] = postingsBytes[nextIntBytePos + j];
                    }
                    nextIntBytePos += 4;

                    int docId = bytesToInt(docIdBytes);
                    //System.out.print("\tdocId="+docId);
                    if (i != 0) {
                        docId += docIdsTermFreqs[i - 1][0];
                    }
                    //System.out.println("\tdocId="+docId);

                    docIdsTermFreqs[i][0] = docId;

                    byte[] termFreqBytes = new byte[4];
                    //System.out.print("startTermFreqBytes="+nextIntBytePos);
                    for (int j = 0; j < termFreqBytes.length; j++) {
                        termFreqBytes[j] = postingsBytes[nextIntBytePos + j];
                    }
                    nextIntBytePos += 4;
                    int termFreq = bytesToInt(termFreqBytes);
                    //System.out.println("\ttermFreq="+termFreq);
                    docIdsTermFreqs[i][1] = termFreq;

                    //postings
                    //System.out.println("SIze of postings=" + (4 * termFreq));
                    nextIntBytePos = nextIntBytePos + (4 * termFreq);
                }
                return docIdsTermFreqs;
            }
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * Responsible for returning and converting a series or bytes to their
     * respective equivalents (id , and positions)
     * @param term the term to retrieve the postings with positions for
     * @return return the final list of postings for the term
     */
    @Override
    public List<Posting> getPostingsWithPositions(String term) {
        try {
            long postingPos = binarySearchVocabTable(term);
            if (postingPos != -1) {
                byte[] numOfDocsBytes = new byte[4];
                for (int i = 0; i < numOfDocsBytes.length; i++) {
                    numOfDocsBytes[i] = postingsBytes[(int) postingPos + i];
                }
                int numOfDocs = bytesToInt(numOfDocsBytes);

                //Posting[] postings = new Posting[numOfDocs];
                ArrayList<Posting> postings = new ArrayList<>();
                int nextIntBytePos = (int) postingPos + 4;

                for (int i = 0; i < numOfDocs; i++) {
                    byte[] docIdBytes = new byte[4];
                    for (int j = 0; j < docIdBytes.length; j++) {
                        docIdBytes[j] = postingsBytes[nextIntBytePos + j];
                    }
                    nextIntBytePos += 4;
                    int docId = bytesToInt(docIdBytes);
                    //System.out.print("docId="+docId);

                    if (i != 0) {
                        docId += postings.get(i - 1).getDocumentId();
                    }

                    //System.out.print("\tdocId="+docId);

                    postings.add(new Posting(docId));

                    byte[] termFreqBytes = new byte[4];
                    for (int j = 0; j < termFreqBytes.length; j++) {
                        termFreqBytes[j] = postingsBytes[nextIntBytePos + j];
                    }
                    nextIntBytePos += 4;
                    int termFreq = bytesToInt(termFreqBytes);
                    //System.out.println("\ttermFreq="+termFreq);

                    for (int j = 0; j < termFreq; j++) {
                        byte[] posBytes = new byte[4];
                        for (int k = 0; k < posBytes.length; k++) {
                            posBytes[k] = postingsBytes[nextIntBytePos + k];
                        }
                        nextIntBytePos += 4;
                        int pos = bytesToInt(posBytes);
                        //System.out.println("posting: "+pos);

                        if (j != 0) {
                            //System.out.println("last posting:"+postings.get(i).getPositions().get(postings.get(i).getPositions().size()-1));
                            pos += postings.get(i).getPositions().get(postings.get(i).getPositions().size() - 1);
                        }
                        //System.out.print("posting: "+pos+",");

                        postings.get(i).addPosition(pos);
                        //System.out.println();
                    }
                    //System.out.print(term+":");
                    //System.out.println(postings);

                }
                return postings;
            }
        } catch (IOException e) {
        }
        return null;
    }

    /**
     *  Binary searches the vocabTable
     * @param term term to binary search the vocabtable.bin file for
     * @return return the byte value at which the term starts at in the vocabTable
     * @throws IOException
     */
    private long binarySearchVocabTable(String term) throws IOException {
        int lowVTAIndex = 0;
        int highVTAIndex = (vocabTableBytes.length / 16) - 1;
        int maxVTAIndex = highVTAIndex;
        int midVTAIndex = -1;
        int midVTByteIndex = -1;
        boolean foundWord = false;


        while (lowVTAIndex <= highVTAIndex) {
            String word = "";
            midVTAIndex = (lowVTAIndex + highVTAIndex) / 2;
            midVTByteIndex = midVTAIndex * 16;

            byte[] midLongBytes = new byte[8];
            for (int i = 0; i < midLongBytes.length; i++) {
                midLongBytes[i] = vocabTableBytes[midVTByteIndex + i];
            }
            long midVocabStartIndex = DiskPositionalIndex.bytesToLong(midLongBytes);
            //System.out.println("midVocabStartIndex="+midVocabStartIndex);

            midVTAIndex++;
            midVTByteIndex = midVTAIndex * 16;

            //System.out.println("lowVTAIndex="+lowVTAIndex);
            //System.out.println("highVTAIndex="+highVTAIndex);

            if (lowVTAIndex == highVTAIndex && lowVTAIndex == maxVTAIndex) {
                byte[] vocabBytes = new byte[vocabFileBytes.length - (int) midVocabStartIndex];
                for (int i = 0; i < vocabBytes.length; i++) {
                    vocabBytes[i] = vocabFileBytes[(int) midVocabStartIndex + i];
                }
                word = new String(vocabBytes);
            } else {
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
            //System.out.println("word=" + word);

            if (term.compareTo(word) > 0) {
                //System.out.println("term is in the last half");
                //term is in the last half
                //highVTAIndex = 0;
                lowVTAIndex = midVTAIndex;

            } else if (term.compareTo(word) < 0) {
                //System.out.println("term is in the first half");
                //term is in the first half
                highVTAIndex = midVTAIndex - 2;
            } else {
                //System.out.println("Term was found at index " + (midVTAIndex - 1));
                foundWord = true;
                break;
            }
        }
        if (foundWord) {
            byte[] postingPos = new byte[8];
            for (int i = 0; i < postingPos.length; i++) {
                postingPos[i] = vocabTableBytes[(midVTAIndex - 1) * 16 + i + 8];
            }
            return bytesToLong(postingPos);
        }
        return -1;
    }

    /**
     * Converts a series of bytes to a long
     * @param bytes bytes to be converted
     * @return long representation of the bytes
     */
    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    /**
     *  Converts a series of bytes to a int
     * @param bytes bytes to be converted
     * @return int representation of the bytes
     */
    public static int bytesToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getInt();
    }

    /**
     *  Converts a series of bytes to a double
     * @param bytes bytes to be converted
     * @return double representation of the bytes
     */
    public static double bytesToDouble(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getDouble();
    }

    /**
     * Retrieve the KGram index
     * @return KGram index
     */
    public KGramIndex getKGramIndex() {
        return kgi;
    }

    /**
     * Returns a sorted list of vocabulary from reading and converting
     * series of bytes from the vocab.bin file
     * @return the entire vocabulary represented as a list of strings
     */
    @Override
    public List<String> getVocabulary() {
        List<String> result = new ArrayList<>();
        try {
            int totalWords = (vocabTableBytes.length / 8) / 2;
            int index = 0;
            String word = "";

            for (int i = 0; i < totalWords; i++) {
                byte[] vocabBytesPos = new byte[8];
                for (int j = 0; j < vocabBytesPos.length; j++) {
                    vocabBytesPos[j] = vocabTableBytes[index + j];
                }
                long vocabPos = bytesToLong(vocabBytesPos);
                index += 16;

                if (index < vocabTableBytes.length) {
                    byte[] nextVocabBytePos = new byte[8];
                    for (int j = 0; j < nextVocabBytePos.length; j++) {
                        nextVocabBytePos[j] = vocabTableBytes[index + j];
                    }
                    long nextVocabPos = bytesToLong(nextVocabBytePos);
                    long vocabLength = nextVocabPos - vocabPos;

                    byte[] vocabBytes = new byte[(int) vocabLength];
                    for (int j = 0; j < vocabBytes.length; j++) {
                        vocabBytes[j] = vocabFileBytes[(int) vocabPos + j];
                    }
                    word = new String(vocabBytes);
                    result.add(word);

                } else {
                    byte[] vocabBytes = new byte[vocabFileBytes.length - (int) vocabPos];
                    for (int j = 0; j < vocabBytes.length; j++) {
                        vocabBytes[j] = vocabFileBytes[(int) vocabPos + j];
                    }
                    word = new String(vocabBytes);
                    result.add(word);
                }
            }
        } catch (Exception e) {

        }
        //System.out.println("size:"+result.size());
        return result;
    }

    /**
     * implemented from index interface
     * @param types HashSet<String> - unique list of all the types within an Index
     */
    @Override
    public void addToKGI(HashSet<String> types) {
        return;
    }

    /**
     * implemented from index interface
     * @param term String - the wildcard
     * @return
     */
    @Override
    public String[] getWildcardMatches(String term) {
        return kgi.getWildcardMatches(term);
    }

    /**
     * implemented from index interface
     * @return
     */
    @Override
    public TokenProcessor getProcessor() {
        return this.processor;
    }
}
