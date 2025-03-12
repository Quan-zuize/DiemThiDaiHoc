package crawl.point.demo.service;

import crawl.point.demo.entity.FieldOfStudy;
import crawl.point.demo.entity.PointByYear;
import crawl.point.demo.entity.FieldStats;
import crawl.point.demo.entity.University;
import crawl.point.demo.repository.UniversityRepository;
import crawl.point.demo.utils.Constant;
import crawl.point.demo.utils.PointTransfer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FieldStatsService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UniversityRepository universityRepository;

    public void calculateAndStoreFieldStats() {
        List<PointByYear> tsaPoints = mongoTemplate.findAll(PointByYear.class, Constant.TSA_NAME);
        List<PointByYear> thptPoints = mongoTemplate.findAll(PointByYear.class, Constant.THPT_NAME);

        Map<String, List<Double>> tsaFieldPoints = new HashMap<>();
        Map<String, Set<String>> tsaUniversities = new HashMap<>();

        Map<String, List<Double>> thptFieldPoints = new HashMap<>();
        Map<String, Set<String>> thptUniversities = new HashMap<>();

        Map<String, String> fieldToName = new HashMap<>();

        // Xử lý TSA
        for (PointByYear point : tsaPoints) {
            if (point.getYear() != 2024) continue;

            String universityName = getUniversityName(point.getUniversityCode());

            for (FieldOfStudy field : point.getFieldOfStudies()) {
                double score = PointTransfer.transferPoint(field.getPoint());

                tsaFieldPoints.computeIfAbsent(field.getFieldId(), k -> new ArrayList<>()).add(score);
                tsaUniversities.computeIfAbsent(field.getFieldId(), k -> new HashSet<>()).add(universityName);
                fieldToName.putIfAbsent(field.getFieldId(), field.getFieldName());
            }
        }

        // Xử lý THPT
        for (PointByYear point : thptPoints) {
            if (point.getYear() != 2024) continue;

            String universityName = getUniversityName(point.getUniversityCode());

            for (FieldOfStudy field : point.getFieldOfStudies()) {
                double score = Double.parseDouble(field.getPoint());

                thptFieldPoints.computeIfAbsent(field.getFieldId(), k -> new ArrayList<>()).add(score);
                thptUniversities.computeIfAbsent(field.getFieldId(), k -> new HashSet<>()).add(universityName);
                fieldToName.putIfAbsent(field.getFieldId(), field.getFieldName());
            }
        }

        // Lưu vào collection "field_stats"
        List<FieldStats> statsList = new ArrayList<>();
        for (String fieldId : fieldToName.keySet()) {
            FieldStats stats = new FieldStats();
            stats.setFieldId(fieldId);
            stats.setFieldName(fieldToName.get(fieldId));

            // Xử lý báo cáo TSA
            if (tsaFieldPoints.containsKey(fieldId)) {
                List<Double> scores = tsaFieldPoints.get(fieldId);
                Set<String> universities = tsaUniversities.get(fieldId);

                if (universities.size() == 1) {
                    stats.setTsaReport(universities.iterator().next() + " - " + scores.get(0));
                } else {
                    double minPoint = Collections.min(scores);
                    double maxPoint = Collections.max(scores);
                    stats.setTsaReport(minPoint + " - " + maxPoint);
                }
            }

            // Xử lý báo cáo THPT
            if (thptFieldPoints.containsKey(fieldId)) {
                List<Double> scores = thptFieldPoints.get(fieldId);
                Set<String> universities = thptUniversities.get(fieldId);

                if (universities.size() == 1) {
                    stats.setThptReport(universities.iterator().next() + " - " + scores.get(0));
                } else {
                    double minPoint = Collections.min(scores);
                    double maxPoint = Collections.max(scores);
                    stats.setThptReport(minPoint + " - " + maxPoint);
                }
            }

            statsList.add(stats);
        }

        // Xóa dữ liệu cũ và lưu dữ liệu mới
        mongoTemplate.dropCollection(FieldStats.class);
        mongoTemplate.insertAll(statsList);

        System.out.println("Cập nhật dữ liệu FieldStats thành công!");
    }

    private String getUniversityName(int universityCode) {
        University uni = universityRepository.findById(universityCode).orElse(null);
        return uni != null ? uni.getName() : "Unknown";
    }

    //    @PostConstruct
    //    public void scheduledUpdate() {
    //        calculateAndStoreFieldStats();
    //    }
}
