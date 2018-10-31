package cecs429.index;

import cecs429.text.TokenProcessor;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.nio.file.Path;

public class DiskPositionalIndex implements Index {
    private FileInputStream vocabTableFile, vocabFile, postingsFile;
    private DataInputStream vocabTableData, vocabData, postingsData;

    public DiskPositionalIndex(Path indexFolder) {
        try {
            vocabTableFile = new FileInputStream(indexFolder.toString() + "/index/vocabTable.bin");
            vocabTableData = new DataInputStream(vocabTableFile);
            vocabFile = new FileInputStream(indexFolder.toString() + "/index/vocab.bin");
            postingsFile = new FileInputStream(indexFolder.toString() + "/index/postings.bin");
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

        int low = 0, midIndex, high = ((int) vocabTableFile.getChannel().size() / 16) - 1, midByteIndex;
        //int numWords = (int) vocabTableFile.getChannel().size() / 16;
        byte[] termBytes = term.getBytes();
        byte[] vocabTableBytes = new byte[(int) vocabTableFile.getChannel().size()];
        vocabTableFile.read(vocabTableBytes);

        while (low <= high) {
            midIndex = (low + high) / 2;
            midByteIndex = midIndex * 16;
            long midStart = vocabTableBytes[midByteIndex];
            byte[] temp = new byte[8];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = vocabTableBytes[midByteIndex + i];
            }
            long midVocabIndex = bytesToLong(temp);

            midIndex++;
            midByteIndex = midIndex * 16;

            byte[] temp2 = new byte[8];
            for (int i = 0; i < temp2.length; i++) {
                temp2[i] = vocabTableBytes[midByteIndex + i];
            }
            long nextMidPointIndex = bytesToLong(temp2);
            long wordLength = nextMidPointIndex - midVocabIndex;
            byte[] vocabFileBytes = new byte[(int) vocabFile.getChannel().size()];
            vocabFile.read(vocabFileBytes);
            byte[] words = new byte[(int) wordLength];
            for (int i = 0; i < words.length; i++) {
                words[i] = vocabFileBytes[(int) midVocabIndex + i];
            }
            String word = new String(words);
            if (term.compareTo(word) > 1) {
                low = midIndex++;
            } else if (term.compareTo(word) < 1) {
                high = midIndex--;
            } else {
                byte[] postingPos = new byte[8];
                for (int i = 0; i < postingPos.length; i++) {
                    postingPos[i] = vocabTableBytes[midByteIndex + 8 + i];
                }
                return bytesToLong(postingPos);
            }
        }
        return -1;
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
