package org.apache.solr.search.facet;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.SyntaxError;

public class FacetTopParser extends FacetParser<FacetQuery> {
    private SolrQueryRequest req;

    public FacetTopParser(SolrQueryRequest req) {
        super(null, "facet");
        this.facet = new FacetQuery();
        this.req = req;
    }

    @Override
    public FacetQuery parse(Object args) throws SyntaxError {
        parseSubs(args);
        return facet;
    }

    @Override
    public SolrQueryRequest getSolrRequest() {
        return req;
    }

    @Override
    public IndexSchema getSchema() {
        return req.getSchema();
    }
}
