package searchengine.model.enums;

public enum IndexingStatus {
    INDEXING("INDEXING"),
    INDEXED("INDEXED"),
    FAILED("FAILED");

    private final String value;


    IndexingStatus(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
