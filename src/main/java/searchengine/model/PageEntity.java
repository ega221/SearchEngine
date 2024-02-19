package searchengine.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "page", indexes = @Index(columnList = "path", name = "path_index"))
@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "FK_page_site"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SiteEntity site;
    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String path;
    @Column(nullable = false)
    private int code;
    @Column(columnDefinition = "mediumtext", nullable = false)
    private String content;
}
