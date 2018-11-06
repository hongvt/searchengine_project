package cecs429.index;

import cecs429.text.TokenProcessor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.nio.file.Path;

public class DiskPositionalIndex implements Index {

    private byte[] vocabFileBytes, vocabTableBytes, postingsBytes;

    private TokenProcessor processor;

    public DiskPositionalIndex(Path corpusFolder, TokenProcessor processor) {
        this.processor = processor;
        try {
            File vocabFile = new File(corpusFolder.toString()+"/index/vocab.bin");
            InputStream vocabIS = new FileInputStream(vocabFile);
            DataInputStream vocabDIS = new DataInputStream(vocabIS);
            vocabFileBytes = new byte[(int)vocabFile.length()];
            vocabDIS.read(vocabFileBytes);

            File vocabTableFile = new File(corpusFolder.toString()+"/index/vocabTable.bin");
            InputStream vocabTableIS = new FileInputStream(vocabTableFile);
            DataInputStream vocabTableDIS = new DataInputStream(vocabTableIS);
            vocabTableBytes = new byte[(int)vocabTableFile.length()];
            vocabTableDIS.read(vocabTableBytes);

            File postingsFile = new File(corpusFolder.toString()+"/index/vocabTable.bin");
            InputStream postingsIS = new FileInputStream(postingsFile);
            DataInputStream postingsDIS = new DataInputStream(postingsIS);
            postingsBytes = new byte[(int)postingsFile.length()];
            postingsDIS.read(postingsBytes);
        } catch (IOException e) {
            System.out.println("IO exception");
            e.printStackTrace();
        }
    }

    @Override
    public int[][] getPostingsNoPositions(String term)
    {
        try {
            long postingPos = binarySearchVocabTable(term);
            byte[] numOfDocsBytes = new byte[4];
            for (int i = 0; i < numOfDocsBytes.length; i++) {
                numOfDocsBytes[i] = postingsBytes[(int) postingPos + i];
            }
            int numOfDocs = bytesToInt(numOfDocsBytes);

            int[][] docIdsTermFreqs = new int[numOfDocs][2];
            int nextIntBytePos = (int) postingPos + 4;

            for (int i = 0; i < numOfDocs; i++) {
                byte[] docIdBytes = new byte[4];
                for (int j = 0; j < docIdBytes.length; j++) {
                    docIdBytes[i] = postingsBytes[nextIntBytePos + j];
                }
                nextIntBytePos += 4;
                int docId = bytesToInt(docIdBytes);

                if (i == 0) {
                    docIdsTermFreqs[i][0] = docId;
                } else {
                    docIdsTermFreqs[i][0] = docId + docIdsTermFreqs[i - 1][0];
                }

                byte[] termFreqBytes = new byte[4];
                for (int j = 0; j < termFreqBytes.length; j++) {
                    termFreqBytes[i] = termFreqBytes[nextIntBytePos + j];
                }
                nextIntBytePos += 4;
                int termFreq = bytesToInt(termFreqBytes);
                docIdsTermFreqs[i][1] = termFreq;

                nextIntBytePos += (4 * termFreq);
            }
            return docIdsTermFreqs;
        }
        catch (IOException e) {};
        return null;
    }

    @Override
    public List<Posting> getPostingsWithPositions(String term){
        try
        {
            long postingPos = binarySearchVocabTable(term);
            byte[] numOfDocsBytes = new byte[4];
            for (int i = 0; i < numOfDocsBytes.length; i++) {
                numOfDocsBytes[i] = postingsBytes[(int)postingPos + i];
            }
            int numOfDocs = bytesToInt(numOfDocsBytes);

            //Posting[] postings = new Posting[numOfDocs];
            ArrayList<Posting> postings = new ArrayList<>();
            int nextIntBytePos = (int)postingPos + 4;

            for (int i = 0; i < numOfDocs; i++)
            {
                byte[] docIdBytes = new byte[4];
                for (int j = 0; j < docIdBytes.length; j++) {
                    docIdBytes[i] = postingsBytes[nextIntBytePos + j];
                }
                nextIntBytePos += 4;
                int docId = bytesToInt(docIdBytes);

                if (i == 0) {
                    postings.add(new Posting(docId));
                }
                else
                {
                    postings.add(new Posting(docId+postings.get(i-1).getDocumentId()));
                }

                byte[] termFreqBytes = new byte[4];
                for (int j = 0; j < termFreqBytes.length; j++) {
                    termFreqBytes[i] = termFreqBytes[nextIntBytePos + j];
                }
                nextIntBytePos += 4;
                int termFreq = bytesToInt(termFreqBytes);

                for (int j = 0; j < termFreq; j++)
                {
                    byte[] posBytes = new byte[4];
                    for (int k = 0; k < posBytes.length; k++) {
                        posBytes[i] = posBytes[nextIntBytePos + k];
                    }
                    nextIntBytePos += 4;
                    int pos = bytesToInt(posBytes);

                    if (i == 0) {
                        postings.get(i).addPosition(pos);
                    }
                    else
                    {
                        postings.get(i).addPosition(pos+postings.get(i).getPositions().get(postings.get(i).getPositions().size()-1));
                    }
                }
            }
            return postings;
        }
        catch (IOException e) {};
        return null;
    }

    private long binarySearchVocabTable(String term) throws IOException {
        int lowVTAIndex = 0;
        int highVTAIndex = (vocabTableBytes.length / 16) - 1;
        int midVTAIndex;
        int midVTByteIndex = 0;

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
        byte[] postingPos = new byte[8];
        for (int i = 0; i < postingPos.length; i++) {
            postingPos[i] = vocabTableBytes[(midVTByteIndex/16) + 8 + i];
        }
        return bytesToLong(postingPos);
    }

    private static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    private static int bytesToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getInt();
    }

    public static double bytesToDouble(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getDouble();
    }

    @Override
    public List<String> getVocabulary() {
        return null;
    }

    @Override
    public void addToKGI(HashSet<String> types) { return; }

    @Override
    public String[] getWildcardMatches(String term) { return null; }

    @Override
    public TokenProcessor getProcessor() { return this.processor; }
}
