package searchengine.services.impl;

import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.indexing.IndexingResponseError;
import searchengine.dto.indexing.IndexingResponseOk;
import searchengine.services.IndexingService;

@Service
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sitesList;
    private boolean isIndexing = false;

    public IndexingServiceImpl(SitesList sitesList) {
        this.sitesList = sitesList;
    }

    @Override
    public IndexingResponse startIndexing() {
        if (isIndexing) {
            return new IndexingResponseError(false,"Индексация уже запущена");
        } else {
            isIndexing = true;
            return new IndexingResponseOk();
        }

    }

    @Override
    public IndexingResponse stopIndexing() {
        if (!isIndexing) {
            return new IndexingResponseError(false,"Индексация не запущена");
        } else {
            isIndexing = false;
            return new IndexingResponseOk();
        }
    }
}
