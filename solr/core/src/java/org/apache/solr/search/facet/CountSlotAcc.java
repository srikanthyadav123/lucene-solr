package org.apache.solr.search.facet;

public abstract class CountSlotAcc extends SlotAcc {
    public CountSlotAcc(FacetContext fcontext) {
        super(fcontext);
    }

    public abstract void incrementCount(int slot, int count);

    public abstract int getCount(int slot);
}
