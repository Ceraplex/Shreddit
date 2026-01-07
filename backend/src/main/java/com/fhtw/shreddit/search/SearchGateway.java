package com.fhtw.shreddit.search;

import java.util.List;

public interface SearchGateway {
    List<SearchHit> search(String query);
}
