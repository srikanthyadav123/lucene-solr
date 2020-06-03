/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.search.facet;

import org.apache.solr.SolrTestCaseHS;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestJsonFacetsWithNestedObjects extends SolrTestCaseHS{

  @BeforeClass
  public static void beforeTests() throws Exception {
    initCore("solrconfig-tlog.xml","schema_latest.xml");
    indexBooksAndReviews();
  }

  private static void indexBooksAndReviews() throws Exception {
    final Client client = Client.localClient();
    client.deleteByQuery("*:*", null);

    SolrInputDocument book1 = sdoc(
        "id",         "book1",
        "type_s",     "book",
        "title_t",    "The Way of Kings",
        "author_s",   "Brandon Sanderson",
        "cat_s",      "fantasy",
        "pubyear_i",  "2010",
        "publisher_s","Tor");

    book1.addChildDocument(
        sdoc(
            "id", "book1_c1",
            "type_s", "review",
            "review_dt", "2015-01-03T14:30:00Z",
            "stars_i", "5",
            "author_s", "yonik",
            "comment_t", "A great start to what looks like an epic series!"));

    book1.addChildDocument(
        sdoc(
            "id", "book1_c2",
            "type_s", "review",
            "review_dt", "2014-03-15T12:00:00Z",
            "stars_i", "3",
            "author_s", "dan",
            "comment_t", "This book was too long."));
    client.add(book1, null);

    SolrInputDocument book2 = sdoc(
        "id",         "book2",
        "type_s",     "book",
        "title_t",    "Snow Crash",
        "author_s",   "Neal Stephenson",
        "cat_s",      "sci-fi",
        "pubyear_i",  "1992",
        "publisher_s","Bantam");

    book2.addChildDocument(
        sdoc(
            "id", "book2_c1",
            "type_s", "review",
            "review_dt", "2015-01-03T14:30:00Z",
            "stars_i", "5",
            "author_s", "yonik",
            "comment_t", "Ahead of its time... I wonder if it helped inspire The Matrix?"));

    book2.addChildDocument(
        sdoc(
            "id", "book2_c2",
            "type_s", "review",
            "review_dt", "2015-04-10T9:00:00Z",
            "stars_i", "2",
            "author_s", "dan",
            "comment_t", "A pizza boy for the Mafia franchise? Really?"));

    book2.addChildDocument(
        sdoc(
            "id", "book2_c3",
            "type_s", "review",
            "review_dt", "2015-06-02T00:00:00Z",
            "stars_i", "4",
            "author_s", "mary",
            "comment_t", "Neal is so creative and detailed! Loved the metaverse!"));

    client.add(book2, null);
    client.commit();
  }

  /**
   * Example from http://yonik.com/solr-nested-objects/
   * The main query gives us a document list of reviews by author_s:yonik
   * If we want to facet on the book genre (cat_s field) then we need to
   * switch the domain from the children (type_s:reviews) to the parents (type_s:books).
   *
   * And we get a facet over the books which yonik reviewed
   *
   * Note that regardless of which direction we are mapping
   * (parents to children or children to parents),
   * we provide a query that defines the complete set of parents in the index.
   * In these examples, the parent filter is “type_s:book”.
   */
  @Test
  public void testFacetingOnParents() throws Exception {
    final Client client = Client.localClient();
    ModifiableSolrParams p = params("rows","10");
    client.testJQ(params(p, "q", "author_s:yonik", "fl", "id", "fl" , "comment_t"
        , "json.facet", "{" +
            "  genres: {" +
            "    type:terms," +
            "    field:cat_s," +
            "    domain: { blockParent : \"type_s:book\" }" +
            "  }" +
            "}"
        )
        , "response=={numFound:2,start:0,docs:[" +
            "      {id:book1_c1," +
            "        comment_t:\"A great start to what looks like an epic series!\"}," +
            "      {id:book2_c1," +
            "        comment_t:\"Ahead of its time... I wonder if it helped inspire The Matrix?\"}]}"
        , "facets=={ count:2," +
            "genres:{buckets:[ {val:fantasy, count:1}," +
            "                  {val:sci-fi,  count:1}]}}"
    );
  }

  /**
   * Example from http://yonik.com/solr-nested-objects/
   * Now lets say we’re displaying the top sci-fi and fantasy books,
   * and we want to find out who reviews the most books out of our selection.
   * Since our root implicit facet bucket (formed by the query and filters)
   * consists of parent documents (books),
   * we need to switch the facet domain to the children for the author facet.
   *
   * Note that regardless of which direction we are mapping
   * (parents to children or children to parents),
   * we provide a query that defines the complete set of parents in the index.
   * In these examples, the parent filter is “type_s:book”.
   */
  @Test
  public void testFacetingOnChildren() throws Exception {
    final Client client = Client.localClient();
    ModifiableSolrParams p = params("rows","10");
    client.testJQ(params(p, "q", "cat_s:(sci-fi OR fantasy)", "fl", "id", "fl" , "title_t"
        , "json.facet", "{" +
            "  top_reviewers: {" +
            "    type:terms," +
            "    field:author_s," +
            "    domain: { blockChildren : \"type_s:book\" }" +
            "  }" +
            "}"
        )
        , "response=={numFound:2,start:0,docs:[" +
            "      {id:book1," +
            "        title_t:\"The Way of Kings\"}," +
            "      {id:book2," +
            "        title_t:\"Snow Crash\"}]}"
        , "facets=={ count:2," +
            "top_reviewers:{buckets:[ {val:dan,     count:2}," +
            "                         {val:yonik,   count:2}," +
            "                         {val:mary,    count:1} ]}}"
    );
  }


  /**
   * Explicit filter exclusions for rolled up child facets
   */
  @Test
  public void testExplicitFilterExclusions() throws Exception {
    final Client client = Client.localClient();
    ModifiableSolrParams p = params("rows","10");
    client.testJQ(params(p, "q", "{!parent which=type_s:book}comment_t:* %2Bauthor_s:yonik %2Bstars_i:(5 3)"
        , "fl", "id", "fl" , "title_t"
        , "json.facet", "{" +
            "  comments_for_author: {" +
            "    type:query," +
            //note: author filter is excluded
            "    q:\"comment_t:* +stars_i:(5 3)\"," +
            "    domain: { blockChildren : \"type_s:book\" }," +
            "    facet:{" +
            "      authors:{" +
            "        type:terms," +
            "        field:author_s," +
            "        facet: {" +
            "           in_books: \"unique(_root_)\" }}}}," +
            "  comments_for_stars: {" +
            "    type:query," +
            //note: stars_i filter is excluded
            "    q:\"comment_t:* +author_s:yonik\"," +
            "    domain: { blockChildren : \"type_s:book\" }," +
            "    facet:{" +
            "      stars:{" +
            "        type:terms," +
            "        field:stars_i," +
            "        facet: {" +
            "           in_books: \"unique(_root_)\" }}}}}" )

        , "response=={numFound:2,start:0,docs:[" +
            "      {id:book1," +
            "        title_t:\"The Way of Kings\"}," +
            "      {id:book2," +
            "        title_t:\"Snow Crash\"}]}"
        , "facets=={ count:2," +
            "comments_for_author:{" +
            "  count:3," +
            "  authors:{" +
            "    buckets:[ {val:yonik,  count:2, in_books:2}," +
            "              {val:dan,    count:1, in_books:1} ]}}," +
            "comments_for_stars:{" +
            "  count:2," +
            "  stars:{" +
            "    buckets:[ {val:5, count:2, in_books:2} ]}}}"
    );
  }

  @Test
  public void testUniqueBlock() throws Exception {
    final Client client = Client.localClient();
    ModifiableSolrParams p = params("rows","0");
    client.testJQ(params(p, "q", "{!parent tag=top which=type_s:book v=$childquery}"
        , "childquery", "comment_t:*"
        , "fl", "id", "fl" , "title_t"
        , "json.facet", "{" +
            "  types: {" +
            "    domain: { blockChildren:\"type_s:book\"" +
            "            }," +
            "    type:terms," +
            "    field:type_s,"
            + "  limit:-1," +
            "    facet: {" +
            "           in_old__books: \"unique(_root_)\", " +
            "           in_books: \"uniqueBlock(_root_)\" }" +
            "  }" +
            "}" )

        , "response=={numFound:2,start:0,docs:[]}"
        , "facets=={ count:2," +
            "types:{" +
            "    buckets:[ {val:review,    count:5, in_old__books:2, in_books:2} ]}" +
            "}"
    );
  }

}
