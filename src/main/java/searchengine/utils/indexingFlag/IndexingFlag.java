package searchengine.utils.indexingFlag;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class IndexingFlag {
    private AtomicBoolean isIndexingAllowed;

    public IndexingFlag() {
        this.isIndexingAllowed = new AtomicBoolean(true);
    }

    public boolean isIndexingAllowed() {
        return isIndexingAllowed.get();
    }

    public void stopIndexing() {
        isIndexingAllowed.set(false);
    }

    public void allowIndexing() {
        isIndexingAllowed.set(true);
    }
}
