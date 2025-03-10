package crawl.point.demo.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "University")
public class University {
    int id;
    String code;
    String name;
    String alias;
}
