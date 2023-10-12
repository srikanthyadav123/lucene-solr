package org.apache.solr.search.facet;

import java.io.IOException;
import java.util.Arrays;

public class CountSlotArrAcc extends CountSlotAcc {
    int[] result;

    public CountSlotArrAcc(FacetContext fcontext, int numSlots) {
        super(fcontext);
        result = new int[numSlots];
    }

    @Override
    public void collect(int doc, int slotNum) { // TODO: count arrays can use fewer bytes based on the number of docs in
        // the base set (that's the upper bound for single valued) - look at ttf?
        result[slotNum]++;
    }

    @Override
    public int compare(int slotA, int slotB) {
        return Integer.compare(result[slotA], result[slotB]);
    }

    @Override
    public Object getValue(int slotNum) throws IOException {
        return result[slotNum];
    }

    public void incrementCount(int slot, int count) {
        result[slot] += count;
    }

    public int getCount(int slot) {
        return result[slot];
    }

    // internal and expert
    int[] getCountArray() {
        return result;
    }

    @Override
    public void reset() {
        Arrays.fill(result, 0);
    }

    @Override
    public void resize(Resizer resizer) {
        resizer.resize(result, 0);
    }
}
