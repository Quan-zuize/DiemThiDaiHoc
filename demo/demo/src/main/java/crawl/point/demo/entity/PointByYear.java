package crawl.point.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointByYear {
    @Id
    String id;
    int universityCode;
    int year;
    List<FieldOfStudy> fieldOfStudies;

    public PointByYear(int universityCode, int year, List<FieldOfStudy> fieldOfStudies) {
        this.universityCode = universityCode;
        this.year = year;
        this.fieldOfStudies = fieldOfStudies;
    }
}
