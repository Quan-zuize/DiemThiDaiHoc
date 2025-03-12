package crawl.point.demo.repository;

import crawl.point.demo.entity.FieldStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldStatsRepository extends MongoRepository<FieldStats, String> {
    Page<FieldStats> findAll(Pageable pageable);
    Page<FieldStats> findByFieldNameContainingIgnoreCase(String fieldName, Pageable pageable);
}

