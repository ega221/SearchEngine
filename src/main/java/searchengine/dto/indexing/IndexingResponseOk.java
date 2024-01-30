package searchengine.dto.indexing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndexingResponseOk extends IndexingResponse{

    public IndexingResponseOk() {
        super(true);
    }
}
