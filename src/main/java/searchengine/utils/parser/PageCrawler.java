package searchengine.utils.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.RecursiveTask;

public class PageCrawler extends RecursiveTask<Boolean> {

    private static final String  CSS_QUERY = "a[href]";
    private static final String ATTRIBUTE_KEY = "href";

    private final SiteEntity site;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final String url;

    private final String path;

    public PageCrawler(SiteEntity site, PageRepository pageRepository, SiteRepository siteRepository, String url) {
        this.site = site;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.url = url;
        this.path = url.substring(site.getUrl().length());
    }

    @Override
    protected Boolean compute() {
        System.out.println("PageCrawler was invoked with link:" + url);
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
                    PageCrawler pageCrawler = new PageCrawler(site, pageRepository, siteRepository, link);
                    pageCrawler.fork();
                    return true;
                }
            }



        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private PageEntity getTransientPageEntity(SiteEntity site, String path, int code, String content) {

        return PageEntity.builder()
                .site(site)
                .path(path)
                .code(code)
                .content(content)
                .build();
    }

    private void saveTransientPage(PageEntity page) {
        pageRepository.save(page);
    }
}
