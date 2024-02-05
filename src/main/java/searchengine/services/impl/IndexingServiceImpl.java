package searchengine.services.impl;

import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.indexing.IndexingResponseError;
import searchengine.dto.indexing.IndexingResponseOk;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.utils.parser.SiteParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    private ExecutorService executorService;
    private List<SiteParser> siteParsers;

    private boolean isIndexing = false;

    public IndexingServiceImpl(SitesList sitesList, SiteRepository siteRepository, PageRepository pageRepository) {
        this.sitesList = sitesList;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }

    @Override
    public IndexingResponse startIndexing() {
        if (isIndexing) {
            return new IndexingResponseError("Индексация уже запущена");
        } else {
            siteParsers = new ArrayList<>();
            executorService = getExecutorService();
            for (Site site : sitesList.getSites()) {
                SiteParser siteParser = new SiteParser(site, siteRepository, pageRepository);
                siteParsers.add(siteParser);
            }
            try {
                executorService.invokeAll(siteParsers);
            } catch (InterruptedException e) {
                return new IndexingResponseError(e.getMessage());
            }
            isIndexing = true;
            return new IndexingResponseOk();
        }

    }

    @Override
    public IndexingResponse stopIndexing() {
        if (!isIndexing) {
            return new IndexingResponseError("Индексация не запущена");
        } else {
            for (SiteParser siteParser : siteParsers) {
                siteParser.stopIndexing();
            }
            executorService.shutdownNow();
            isIndexing = false;
            return new IndexingResponseOk();
        }
    }

    private ExecutorService getExecutorService(int n) {
        return Executors.newFixedThreadPool(n);
    }

    private ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
}
