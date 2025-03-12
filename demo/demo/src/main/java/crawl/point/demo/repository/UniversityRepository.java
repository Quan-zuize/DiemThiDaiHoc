package crawl.point.demo.repository;

import crawl.point.demo.entity.University;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface UniversityRepository extends MongoRepository<University, Integer> {
    List<University> findByIdIn(Collection<Integer> id);
}
