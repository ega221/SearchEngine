package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.PageEntity;

import javax.transaction.Transactional;

public interface PageRepository extends JpaRepository<PageEntity, Integer> {

    boolean existsByPath(String path);

    @Transactional
    @Modifying
    @Query("DELETE FROM PageEntity p WHERE p.site.id = :siteId")
    void deleteBySiteId(@Param("siteId") Integer siteId);
}
