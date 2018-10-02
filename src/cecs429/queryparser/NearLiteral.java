package cecs429.queryparser;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.Milestone1TokenProcessor;

import java.util.ArrayList;
import java.util.List;

public class NearLiteral implements QueryComponent {
    private String leftTerm, rightTerm;
    private int k;
    private Milestone1TokenProcessor processor = new Milestone1TokenProcessor();

    public NearLiteral(String leftTerm, String rightTerm, int k) {
        this.leftTerm = leftTerm;
        this.rightTerm = rightTerm;
        this.k = k;
    }

    private List<Posting> nearMerge(List<Posting> list_one, List<Posting> list_two, int nearK) {
        List<Posting> result = new ArrayList<>();
        int i = 0, j = 0, k, m;

        while (true) {
            if (i == list_one.size() || j == list_two.size())
                return result;
            else if (list_one.get(i).getDocumentId() == list_two.get(j).getDocumentId()) {
                k = 0;
                m = 0;
                while (true) {
                    if (k == list_one.get(i).getPositions().size() || m == list_two.get(j).getPositions().size()) {
                        i++;
                        j++;
                        break;
                    } else if (list_one.get(i).getPositions().get(k) <= list_two.get(j).getPositions().get(m) &&
                            list_one.get(i).getPositions().get(k) + nearK >= list_two.get(j).getPositions().get(m)) {
                        if (result.isEmpty())
                            result.add(new Posting(list_two.get(j).getDocumentId()));
                        else if (result.get(result.size() - 1).getDocumentId() != list_two.get(j).getDocumentId())
                            result.add(new Posting(list_two.get(j).getDocumentId()));
                        k++;
                    } else {
                        if (list_one.get(i).getPositions().get(k) + nearK < list_two.get(j).getPositions().get(m))
                            k++;
                        else
                            m++;
                    }
                }
            } else {
                if (list_one.get(i).getDocumentId() < list_two.get(j).getDocumentId())
                    i++;
                else
                    j++;
            }
        }
    }

    @Override
    public List<Posting> getPostings(Index index) {
        return nearMerge(index.getPostings(processor.getStem(leftTerm)), index.getPostings(processor.getStem(rightTerm)), k);
    }
}
