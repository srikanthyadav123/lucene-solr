package org.apache.solr.search.facet;

import org.apache.solr.handler.component.ResponseBuilder;

import java.util.Map;

// TODO: perhaps factor out some sort of root/parent facet object that doesn't depend
// on stuff like ResponseBuilder, but contains request parameters,
// root filter lists (for filter exclusions), etc?
public class FacetComponentState {
    ResponseBuilder rb;
    Map<String, Object> facetCommands;
    FacetRequest facetRequest;
    boolean isShard;
    Map<String, Object> facetInfo; // _facet_ param: contains out-of-band facet info, mainly for refinement requests

    //
    // Only used for distributed search
    //
    FacetMerger merger;
    FacetMerger.Context mcontext;
}
