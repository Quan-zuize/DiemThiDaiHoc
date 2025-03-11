package crawl.point.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
public class PointByYear {
    int universityCode;
    int year;
    List<FieldOfStudy> fieldOfStudies;
}
