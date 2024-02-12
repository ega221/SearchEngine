package searchengine.utils.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.utils.indexingFlag.IndexingFlag;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.RecursiveAction;

public class PageCrawler extends RecursiveAction {

    private static final String  CSS_QUERY = "a[href]";
    private static final String ATTRIBUTE_KEY = "href";

    private final SiteEntity site;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final String url;
    private final String path;
    private volatile IndexingFlag indexingFlag;


    public PageCrawler(SiteEntity site, PageRepository pageRepository, SiteRepository siteRepository, String url, IndexingFlag indexingFlag) {
        this.site = site;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.url = url;
        this.indexingFlag = indexingFlag;
        this.path = url.substring(site.getUrl().length());
    }

    @Override
    protected void compute() {
        if (indexingFlag.isIndexingAllowed()) {

            System.out.println("PageCrawler was invoked with link:" + url + " in thread: " + Thread.currentThread().getName());
            site.setStatusTime(LocalDateTime.now());
            siteRepository.saveAndFlush(site);

            try {
                Thread.sleep(500);

                Document page = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true)
                        .userAgent("Mozilla/5.0").get();
                String content = page.outerHtml();
                int code = page.connection().response().statusCode();
                PageEntity transientPageEntity = getTransientPageEntity(site, path, code, content);
                PageEntity persistentPageEntity = pageRepository.save(transientPageEntity);
                Elements elements = page.select(CSS_QUERY);

                for (Element element : elements) {
                    String link = element.absUrl(ATTRIBUTE_KEY);
                    String elementPath = link.substring(site.getUrl().length());

                    if (link.startsWith(site.getUrl()) && !pageRepository.existsByPath(elementPath) && !link.contains("#")) {
                        System.out.println(indexingFlag.isIndexingAllowed() + " in site with url: " + link);
                        PageCrawler pageCrawler = new PageCrawler(site, pageRepository, siteRepository, link, indexingFlag);
                        pageCrawler.fork();
                        pageCrawler.join();
                    }
                }



            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println(indexingFlag.isIndexingAllowed());
        }

    }

    private PageEntity getTransientPageEntity(SiteEntity site, String path, int code, String content) {

        return PageEntity.builder()
                .site(site)
                .path(path)
                .code(code)
                .content(content)
                .build();
    }

}
