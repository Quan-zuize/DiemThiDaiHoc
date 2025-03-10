package crawl.point.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@Document(collection = "DiemChuan")
public class PointByYear {
    int universityCode;
    int year;
    List<FieldOfStudy> fieldOfStudies;
}
