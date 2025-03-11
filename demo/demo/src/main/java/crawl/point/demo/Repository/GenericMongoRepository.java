package crawl.point.demo.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface GenericMongoRepository<T, ID> extends MongoRepository<T, ID> {
    // Interface chung, các truy vấn sẽ được implement trong class riêng
}
