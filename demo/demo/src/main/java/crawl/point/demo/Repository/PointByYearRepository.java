package crawl.point.demo.Repository;

import crawl.point.demo.entity.PointByYear;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointByYearRepository extends MongoRepository<PointByYear, String> {
    @Query(value = "{'universityCode': { $in: ?0 }, 'fieldOfStudies.code': ?1 }")
    List<PointByYear> findByUniversityCodeInAndFieldCode(List<Integer> universityCodes, String fieldCode);
}
