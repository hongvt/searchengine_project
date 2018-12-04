package cecs429.classifiers;

import cecs429.index.DiskPositionalIndex;

public class Rocchio {
    private DiskPositionalIndex hamIndex, jayIndex, madIndex;

    public Rocchio(DiskPositionalIndex hamIndex, DiskPositionalIndex jayIndex, DiskPositionalIndex madIndex) {
        this.hamIndex = hamIndex;
        this.jayIndex = jayIndex;
        this.madIndex = madIndex;
    }

    public void normalizeVector() {

    }

}
