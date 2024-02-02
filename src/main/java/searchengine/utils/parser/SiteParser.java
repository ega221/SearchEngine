package searchengine.utils.parser;

import searchengine.config.Site;
import searchengine.model.SiteEntity;
import searchengine.model.enums.IndexingStatus;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

public class SiteParser implements Callable<Boolean> {
    private final Site siteConfig;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    public SiteParser(Site siteConfig, SiteRepository siteRepository, PageRepository pageRepository) {
        this.siteConfig = siteConfig;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }


    @Override
    public Boolean call() throws Exception {
        SiteEntity site = findOrSave(siteConfig);
        return true;
    }

    private SiteEntity findOrSave(Site siteConfig) {
        SiteEntity site = siteRepository.findByUrl(siteConfig.getUrl());
        //TODO разобраться в том, что должно происходить, в случае если сайт находится в БД
        // нужно удалять все страницы этого сайта из БД, то есть проводить переиндексацию.
        // Или нужно просто закончить индексацию сайта, то есть пройти оставшиеся страницы?
        if (site != null) {
            site.setStatus(IndexingStatus.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.saveAndFlush(site);
        } else {
            site = new SiteEntity();
            site.setName(siteConfig.getName());
            site.setUrl(siteConfig.getUrl());
            site.setStatus(IndexingStatus.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.saveAndFlush(site);
        }
        return site;
    }

}
