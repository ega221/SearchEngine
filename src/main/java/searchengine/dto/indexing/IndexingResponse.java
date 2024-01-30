package searchengine.dto.indexing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class IndexingResponse {
    private boolean result;

    public IndexingResponse() {

    }

    public IndexingResponse(boolean result) {
        this.result = result;
    }
}
