package com.fhtw.shreddit.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticsearchSearchGateway implements SearchGateway {
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchSearchGateway.class);

    private final ElasticsearchClient client;
    private final String indexName;

    public ElasticsearchSearchGateway(ElasticsearchClient client, @Value("${elasticsearch.index:documents}") String indexName) {
        this.client = client;
        this.indexName = indexName;
    }

    @Override
    public List<SearchHit> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        try {
            SearchResponse<IndexedDocument> response = client.search(s -> s
                            .index(indexName)
                            .query(q -> q.multiMatch(m -> m
                                    .fields("title^2", "summary", "ocrText", "content")
                                    .query(query)
                                    .fuzziness("AUTO")
                            ))
                            .size(25),
                    IndexedDocument.class);

            List<SearchHit> hits = new ArrayList<>();
            for (Hit<IndexedDocument> hit : response.hits().hits()) {
                Long id = null;
                if (hit.id() != null && !hit.id().isBlank()) {
                    try {
                        id = Long.parseLong(hit.id());
                    } catch (NumberFormatException ignored) {
                        log.warn("SEARCH: ignoring non-numeric hit id={}", hit.id());
                    }
                }
                hits.add(new SearchHit(id, hit.source(), hit.score() != null ? hit.score() : 0.0));
            }
            return hits;
        } catch (Exception e) {
            log.error("SEARCH: Elasticsearch error while querying '{}': {}", query, e.getMessage());
            return List.of();
        }
    }
}
