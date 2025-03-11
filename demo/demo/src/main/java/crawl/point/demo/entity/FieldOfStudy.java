package crawl.point.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldOfStudy {
    @Id
    String fieldId;
    String fieldName;
    String SubjectCombine;
    String point;
    String note;
}
