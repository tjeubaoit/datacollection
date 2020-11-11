package com.datacollection.platform.elastic;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class EsScrollIterator implements Iterator<SearchHit> {

    private final Client client;
    private final int scrollTimeoutInMinutes;
    private SearchResponse searchResp;
    private Iterator<SearchHit> hitIterator;

    public EsScrollIterator(Client client, SearchResponse searchResp, int scrollTimeoutInMinutes) {
        this.client = client;
        this.scrollTimeoutInMinutes = scrollTimeoutInMinutes;
        this.searchResp = searchResp;
        this.hitIterator = searchResp.getHits().iterator();
    }

    @Override
    public boolean hasNext() {
        if (!hitIterator.hasNext()) {
            checkNextScroll();
        }
        return hitIterator.hasNext();
    }

    @Override
    public SearchHit next() {
        SearchHit hit = hitIterator.next();
        if (hit == null) {
            checkNextScroll();
            return hitIterator.next();
        }
        return hit;
    }

    private void checkNextScroll() {
        searchResp = client.prepareSearchScroll(searchResp.getScrollId())
                .setScroll(new TimeValue(scrollTimeoutInMinutes, TimeUnit.MINUTES))
                .execute().actionGet();
        hitIterator = searchResp.getHits().iterator();
    }
}
