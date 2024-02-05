package searchengine.dto.indexing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndexingResponseError extends IndexingResponse{
    private String error;

    public IndexingResponseError(String error) {
        super(false);
        this.error = error;
    }
}
