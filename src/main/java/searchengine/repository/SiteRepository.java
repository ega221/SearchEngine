package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.SiteEntity;

import javax.transaction.Transactional;

public interface SiteRepository extends JpaRepository<SiteEntity, Long> {
    @Transactional
    SiteEntity findByUrl(String url);

}
