package searchengine.utils.parser;

import searchengine.config.Site;
import searchengine.model.SiteEntity;
import searchengine.model.enums.IndexingStatus;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

public class SiteParser implements Callable<Boolean> {
    private final Site siteConfig;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private boolean isParsed = false;

    private SiteEntity site;

    private ForkJoinPool forkJoinPool;

    public SiteParser(Site siteConfig, SiteRepository siteRepository, PageRepository pageRepository) {
        this.siteConfig = siteConfig;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }


    @Override
    public Boolean call() throws Exception {
        site = findOrSave(siteConfig);
        PageCrawler pageCrawler = new PageCrawler(site, pageRepository, siteRepository, site.getUrl());
        forkJoinPool = new ForkJoinPool(4);
        forkJoinPool.invoke(pageCrawler);
        return true;
    }

    private SiteEntity findOrSave(Site siteConfig) {
        site = siteRepository.findByUrl(siteConfig.getUrl());
        if (site != null) {
            // Todo: разобраться с проблемой удаления старых страниц, связанных с сайтом, почему ничего не происходит?
            pageRepository.deleteBySiteId(site.getId());
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

    public void stopIndexing() {
        if (!isParsed) {
            site.setStatus(IndexingStatus.FAILED);
            site.setStatusTime(LocalDateTime.now());
            site.setLastError("Индексация остановлена пользователем");
            site = siteRepository.saveAndFlush(site);
            forkJoinPool.shutdownNow();
        }
    }

}
