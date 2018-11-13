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
public class DiskIndexWriter
{
    private DataOutputStream postData, vocabData, vocabTableData, docWeightsData;

    public DiskIndexWriter(Path indexFolder)
    {
        try
        {
            FileOutputStream postingsFile = new FileOutputStream(indexFolder.toString()+"/index/postings.bin");
            postData = new DataOutputStream(postingsFile);

            FileOutputStream vocabFile = new FileOutputStream(indexFolder.toString()+"/index/vocab.bin");
            vocabData = new DataOutputStream(vocabFile);

            FileOutputStream vocabTableFile = new FileOutputStream(indexFolder.toString()+"/index/vocabTable.bin");
            vocabTableData = new DataOutputStream(vocabTableFile);

            FileOutputStream docWeightsFile = new FileOutputStream(indexFolder.toString()+"/index/docWeights.bin");
            docWeightsData = new DataOutputStream(docWeightsFile);
        }
        catch (IOException e) {System.out.println("IO exception"); e.printStackTrace(); }
    }
    public void writeDocWeight(double docWeight)
    {
        try {
            docWeightsData.writeDouble(docWeight);
        }
        catch (IOException e) {System.out.println("IO exception"); e.printStackTrace(); }
    }

    public void closeDocWeights()
    {
        try
        {
            docWeightsData.close();
        }
        catch (IOException e) {System.out.println("IO exception"); e.printStackTrace(); }
    }

    public void writeIndex(PositionalInvertedIndex positionalInvertedIndex)
    {
        try
        {
            long nextVocabPos = 0;
            long nextPostPos = 0;
            List<String> vocabList = positionalInvertedIndex.getVocabulary();
            for (int i = 0; i < vocabList.size(); i++)
            {
                long startTime = System.currentTimeMillis();
                writeVocabTable(nextVocabPos, nextPostPos);
                nextVocabPos += writeVocab(vocabList.get(i));
                nextPostPos += writePostings(vocabList.get(i), positionalInvertedIndex);
            }
            vocabTableData.flush();
            vocabData.flush();
            postData.flush();
            //closeVocabPostOutput();
        }
        catch (IOException e) { System.out.println("IO exception"); e.printStackTrace(); }

    }

    public void closeVocabPostOutput()
    {
        try
        {
            vocabData.close();
            vocabTableData.close();
            postData.close();
        }
        catch (IOException e) {System.out.println("IO exception"); e.printStackTrace(); }
    }

    private long writePostings(String term, PositionalInvertedIndex positionalInvertedIndex)
    {
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
        }
        catch(IOException e) {System.out.println("in writePostings");}
        return -1;
    }

    private long writeVocab(String term)
    {
        try {
            byte[] termBytes = term.getBytes();
            vocabData.write(termBytes);
            return termBytes.length;
        }
        catch(IOException e) {System.out.println("in writeVocab");}
        return -1;
    }

    private void writeVocabTable(long nextVocabPos, long nextPostPos)
    {
        try {
            vocabTableData.writeLong(nextVocabPos);
            vocabTableData.writeLong(nextPostPos);
        }
        catch(IOException e) {System.out.println("in writeVocabTable"); }
    }
}