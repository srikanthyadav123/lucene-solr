package org.apache.solr.search.facet;

import java.io.IOException;

public class SortSlotAcc extends SlotAcc {
    public SortSlotAcc(FacetContext fcontext) {
        super(fcontext);
    }

    @Override
    public void collect(int doc, int slot) throws IOException {
        // no-op
    }

    public int compare(int slotA, int slotB) {
        return slotA - slotB;
    }

    @Override
    public Object getValue(int slotNum) {
        return slotNum;
    }

    @Override
    public void reset() {
        // no-op
    }

    @Override
    public void resize(Resizer resizer) {
        // sort slot only works with direct-mapped accumulators
        throw new UnsupportedOperationException();
    }
}
