package cecs429.index;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

//to check the .bin files:
//vim <fileName>.bin
//type :
//then copy and paste
//% ! xxd

/**
 * Responsible for writing out the entirety of the vocabulary, vocabulary table, doc weights,
 * and postings of the vocabulary
 * to disk.
 */
public class DiskIndexWriter {
    /**
     * Member variables of the class corresponding to their respective files they are writing
     * to
     */
    private DataOutputStream postData, vocabData, vocabTableData, docWeightsData;

    /**
     *  Constructs and initializes file output streams for their corresponding files
     * @param indexFolder Represents the index directory where all 4 files will be written to
     */
    public DiskIndexWriter(Path indexFolder) {
        try {
            FileOutputStream postingsFile = new FileOutputStream(indexFolder.toString() + "/index/postings.bin");
            postData = new DataOutputStream(postingsFile);

            FileOutputStream vocabFile = new FileOutputStream(indexFolder.toString() + "/index/vocab.bin");
            vocabData = new DataOutputStream(vocabFile);

            FileOutputStream vocabTableFile = new FileOutputStream(indexFolder.toString() + "/index/vocabTable.bin");
            vocabTableData = new DataOutputStream(vocabTableFile);

            FileOutputStream docWeightsFile = new FileOutputStream(indexFolder.toString() + "/index/docWeights.bin");
            docWeightsData = new DataOutputStream(docWeightsFile);
        } catch (IOException e) {
            System.out.println("IO exception");
            e.printStackTrace();
        }
    }

    /**
     *  Responsible for writing out the docWeight to disk
     * @param docWeight Value to be written out
     */
    public void writeDocWeight(double docWeight) {
        try {
            docWeightsData.writeDouble(docWeight);
        } catch (IOException e) {
            System.out.println("IO exception");
            e.printStackTrace();
        }
    }

    /**
     *  Closes file output stream
     */
    public void closeDocWeights() {
        try {
            docWeightsData.close();
        } catch (IOException e) {
            System.out.println("IO exception");
            e.printStackTrace();
        }
    }

    /**
     *  Responsible for writing out the components of the index to disk
     * @param positionalInvertedIndex The filled positional inverted index
     */
    public void writeIndex(PositionalInvertedIndex positionalInvertedIndex) {
        try {
            long nextVocabPos = 0;
            long nextPostPos = 0;
            List<String> vocabList = positionalInvertedIndex.getVocabulary();
            for (int i = 0; i < vocabList.size(); i++) {
                long startTime = System.currentTimeMillis();
                writeVocabTable(nextVocabPos, nextPostPos);
                nextVocabPos += writeVocab(vocabList.get(i));
                nextPostPos += writePostings(vocabList.get(i), positionalInvertedIndex);
            }
            vocabTableData.flush();
            vocabData.flush();
            postData.flush();
            //closeVocabPostOutput();
        } catch (IOException e) {
            System.out.println("IO exception");
            e.printStackTrace();
        }

    }

    /**
     *  Closes the vocab postings output
     */
    public void closeVocabPostOutput() {
        try {
            vocabData.close();
            vocabTableData.close();
            postData.close();
        } catch (IOException e) {
            System.out.println("IO exception");
            e.printStackTrace();
        }
    }

    /**
     *  Responsible for writing out the postings of a given term to disk
     * @param term term within the index's vocabulary
     * @param positionalInvertedIndex retrieve the postings of a term from this index
     * @return the amount of bytes the postings list for a term will take up on disk
     */
    private long writePostings(String term, PositionalInvertedIndex positionalInvertedIndex) {
        try {
            long acc = 0;
            List<Posting> posts = positionalInvertedIndex.getPostingsWithPositions(term);
            int numPostings = posts.size();
            postData.writeInt(numPostings);
            acc += 4;
            for (int i = 0; i < numPostings; i++) {
                Posting posting = posts.get(i);
                int compressedDocId = posting.getDocumentId();
                if (i != 0) {
                    compressedDocId -= posts.get(i - 1).getDocumentId();
                }
                postData.writeInt(compressedDocId);
                acc += 4;
                List<Integer> postingPos = posting.getPositions();
                postData.writeInt(postingPos.size());
                acc += 4;
                for (int j = 0; j < postingPos.size(); j++) {
                    int compressedPos = postingPos.get(j);
                    if (j != 0) {
                        compressedPos -= postingPos.get(j - 1);
                    }
                    postData.writeInt(compressedPos);
                    acc += 4;
                }
            }
            return acc;
        } catch (IOException e) {
            System.out.println("in writePostings");
        }
        return -1;
    }

    /**
     *
     * @param term Term to be written out to disk
     * @return amount of bytes term will take up on disk
     */
    private long writeVocab(String term) {
        try {
            byte[] termBytes = term.getBytes();
            vocabData.write(termBytes);
            return termBytes.length;
        } catch (IOException e) {
            System.out.println("in writeVocab");
        }
        return -1;
    }

    /**
     *
     * @param nextVocabPos  Position of the next vocabulary term to write in disk
     * @param nextPostPos   Position of the next posting to write to disk
     */
    private void writeVocabTable(long nextVocabPos, long nextPostPos) {
        try {
            vocabTableData.writeLong(nextVocabPos);
            vocabTableData.writeLong(nextPostPos);
        } catch (IOException e) {
            System.out.println("in writeVocabTable");
        }
    }
}