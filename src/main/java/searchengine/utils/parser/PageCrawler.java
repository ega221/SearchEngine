package searchengine.utils.parser;

import org.jsoup.Connection;
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
import java.util.concurrent.RecursiveAction;

public class PageCrawler extends RecursiveAction {
    //Todo: Добавить обновление времени статуса для сайта в соответствии с ТЗ.

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
    protected void compute() {
        //System.out.println("PageCrawler was invoked with link:" + url);
        //System.out.println(pageRepository.existsByPath(path));
        site.setStatusTime(LocalDateTime.now());
        siteRepository.saveAndFlush(site);

        try {
            Thread.sleep(500);
            Document page = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true)
                    .userAgent("Mozilla/5.0").get();
            String content = page.outerHtml();
            int code = page.connection().response().statusCode();
            PageEntity transientpageEntity = getTransientPageEntity(site, path, code, content);
            PageEntity persistentPageEntity = pageRepository.save(transientpageEntity);
            Elements elements = page.select(CSS_QUERY);

            for (Element element : elements) {
                String link = element.absUrl(ATTRIBUTE_KEY);
                String elementPath = link.substring(site.getUrl().length());
                //System.out.println("\t" + (link.startsWith(site.getUrl())));
                if (link.startsWith(site.getUrl()) && !pageRepository.existsByPath(elementPath) && !link.contains("#")) {
                    PageCrawler pageCrawler = new PageCrawler(site, pageRepository, siteRepository, link);
                    //System.out.println(link);
                    pageCrawler.fork();
                }
            }



        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
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

    private void saveTransientPage(PageEntity page) {
        pageRepository.save(page);
    }
}
