package crawl.point.demo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "field_stats_2024")
public class FieldStats {
    @Id
    private String fieldId;
    private String fieldName;
    private String tsaReport;
    private String thptReport;
}
