package crawl.point.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Data
@Document(collection = "field_stats_2024")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FieldStats {
    @Id
    private String fieldId;
    private String fieldName;
    private String tsaReport;
    private String thptReport;
    private Map<Integer, Double> tsaUniversityScores;
    private Map<Integer, Double> thptUniversityScores;

    public FieldStats(String fieldId, String fieldName) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
    }

    public void setReport(boolean isTsa, String report) {
        if (isTsa) this.tsaReport = report;
        else this.thptReport = report;
    }

    public void setUniversityScores(boolean isTsa, Map<Integer, Double> universityScores) {
        if (!universityScores.isEmpty()) {
            if (isTsa) this.tsaUniversityScores = universityScores;
            else this.thptUniversityScores = universityScores;
        }
    }
}