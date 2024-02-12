package searchengine.utils.parser;

import searchengine.config.Site;
import searchengine.model.SiteEntity;
import searchengine.model.enums.IndexingStatus;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.utils.indexingFlag.IndexingFlag;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;

public class SiteParser implements Runnable {
    private final Site siteConfig;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private volatile IndexingFlag indexingFlag;
    private SiteEntity site;
    private ForkJoinPool forkJoinPool;

    public SiteParser(Site siteConfig, SiteRepository siteRepository, PageRepository pageRepository, IndexingFlag indexingFlag) {
        this.siteConfig = siteConfig;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.indexingFlag = indexingFlag;
    }


    @Override
    public void run() {
        try {
            site = findOrSave(siteConfig);
            PageCrawler pageCrawler = new PageCrawler(site, pageRepository, siteRepository, site.getUrl(), indexingFlag);
            // ToDo: сделать выбор парралелизма на основе доступных ядер.
            forkJoinPool = new ForkJoinPool(4);
            forkJoinPool.submit(pageCrawler);
            pageCrawler.join();
            if (indexingFlag.isIndexingAllowed()) {
                setIndexedStatus();
                forkJoinPool.shutdown();
            } else {
                stoppedIndexing();
                forkJoinPool.shutdown();
            }

        } catch (CancellationException e) {
            site.setStatus(IndexingStatus.FAILED);
            site.setStatusTime(LocalDateTime.now());
            site.setLastError("Ошибка индексации: " + e.getMessage());
            site = siteRepository.saveAndFlush(site);
        }

    }

    private SiteEntity findOrSave(Site siteConfig) {
        site = siteRepository.findByUrl(siteConfig.getUrl());
        if (site != null) {
            pageRepository.deleteBySiteId(site.getId());
            site.setLastError(null);
            site.setStatus(IndexingStatus.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.saveAndFlush(site);
        } else {
            site = new SiteEntity();
            site.setName(siteConfig.getName());
            site.setUrl(siteConfig.getUrl());
            site.setStatus(IndexingStatus.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            site = siteRepository.saveAndFlush(site);
        }
        return site;
    }

    public void stoppedIndexing() {
        site.setStatus(IndexingStatus.FAILED);
        site.setStatusTime(LocalDateTime.now());
        site.setLastError("Индексация остановлена пользователем");
        site = siteRepository.saveAndFlush(site);
    }

    public void setIndexedStatus() {
        site.setStatus(IndexingStatus.INDEXED);
        site.setStatusTime(LocalDateTime.now());
        siteRepository.saveAndFlush(site);

    }

}
