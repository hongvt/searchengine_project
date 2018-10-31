package cecs429.index;

import cecs429.text.TokenProcessor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.nio.file.Path;

public class DiskPositionalIndex implements Index {

    private byte[] vocabFileBytes, vocabTableBytes;

    public DiskPositionalIndex(Path corpusFolder) {
        try {
            File vocabFile = new File(corpusFolder.toString()+"/vocab.bin");
            InputStream vocabIS = new FileInputStream(vocabFile);
            DataInputStream vocabDIS = new DataInputStream(vocabIS);
            vocabFileBytes = new byte[(int)vocabFile.length()];
            vocabDIS.read(vocabFileBytes);

            File vocabTableFile = new File(corpusFolder.toString()+"/vocabTable.bin");
            InputStream vocabTableIS = new FileInputStream(vocabTableFile);
            DataInputStream vocabTableDIS = new DataInputStream(vocabTableIS);
            vocabTableBytes = new byte[(int)vocabTableFile.length()];
            vocabTableDIS.read(vocabTableBytes);
        } catch (IOException e) {
            System.out.println("IO exception");
            e.printStackTrace();
        }
    }

    @Override
    public List<Posting> getPostings(String term) {

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

    public long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    @Override
    public List<String> getVocabulary() {
        return null;
    }

    @Override
    public void addToKGI(HashSet<String> types) {
        return;
    }

    @Override
    public String[] getWildcardMatches(String term) {
        return new String[0];
    }

    @Override
    public TokenProcessor getProcessor() {
        return null;
    }
}
