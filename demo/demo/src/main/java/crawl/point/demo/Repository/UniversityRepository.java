package crawl.point.demo.Repository;

import crawl.point.demo.entity.University;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public interface UniversityRepository extends MongoRepository<University, String> {
    List<University> findByIdIn(Collection<Integer> id);
}
