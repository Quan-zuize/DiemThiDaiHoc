package crawl.point.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointByYear {
    int universityCode;
    int year;
    List<FieldOfStudy> fieldOfStudies;
}
