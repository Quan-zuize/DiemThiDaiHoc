package crawl.point.demo.service;

import crawl.point.demo.entity.FieldOfStudy;
import crawl.point.demo.entity.PointByYear;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class PointService {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Truy vấn dữ liệu với collection động
     */
    public List<PointByYear> findByUniversityCodeInAndFieldCode(String collectionName, List<Integer> universityCodes, String fieldCode) {
        Query query = new Query();
        query.addCriteria(Criteria.where("universityCode").in(universityCodes)
                .and("fieldOfStudies.code").is(fieldCode));

        return mongoTemplate.find(query, PointByYear.class, collectionName);
    }

    @Async
    public CompletableFuture<List<PointByYear>> getAllPointByYear(String collectionName) {
        return CompletableFuture.completedFuture(mongoTemplate.findAll(PointByYear.class, collectionName));
    }

    @Async
    public void saveField(FieldOfStudy fieldOfStudy, String collectionName) {
        mongoTemplate.save(fieldOfStudy, collectionName);
    }

    @Async
    public void savePointByYear(PointByYear pointByYear, String collectionName) {
        mongoTemplate.save(pointByYear, collectionName);
    }
}
