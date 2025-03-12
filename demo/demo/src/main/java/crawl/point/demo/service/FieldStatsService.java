package crawl.point.demo.service;

import crawl.point.demo.entity.FieldOfStudy;
import crawl.point.demo.entity.PointByYear;
import crawl.point.demo.entity.FieldStats;
import crawl.point.demo.entity.University;
import crawl.point.demo.repository.FieldStatsRepository;
import crawl.point.demo.repository.UniversityRepository;
import crawl.point.demo.utils.Constant;
import crawl.point.demo.utils.PointTransfer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FieldStatsService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UniversityRepository universityRepository;
    @Autowired
    private FieldStatsRepository fieldStatsRepository;

    public Page<FieldStats> getPagedFieldStats(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("fieldName"));

        if (keyword == null || keyword.trim().isEmpty()) {
            return fieldStatsRepository.findAll(pageable);
        }

        return fieldStatsRepository.findByFieldNameContainingIgnoreCase(keyword, pageable);
    }

    public void calculateAndStoreFieldStats() {
        List<PointByYear> tsaPoints = mongoTemplate.findAll(PointByYear.class, Constant.TSA_NAME);
        List<PointByYear> thptPoints = mongoTemplate.findAll(PointByYear.class, Constant.THPT_NAME);

        Map<String, FieldStats> statsMap = new HashMap<>();

        processPoints(tsaPoints, statsMap, true);
        processPoints(thptPoints, statsMap, false);

        mongoTemplate.dropCollection(FieldStats.class);
        mongoTemplate.insertAll(statsMap.values());

        System.out.println("Cập nhật dữ liệu FieldStats thành công!");
    }

    private Map<String, List<Double>> tsaFieldPoints = new HashMap<>();
    private Map<String, List<Double>> thptFieldPoints = new HashMap<>();
    private void processPoints(List<PointByYear> points, Map<String, FieldStats> statsMap, boolean isTsa) {
        for (PointByYear point : points) {
            if (point.getYear() != 2024) continue;

            int universityId = point.getUniversityCode();
            String universityName = getUniversityName(universityId);

            for (FieldOfStudy field : point.getFieldOfStudies()) {
                double score = isTsa ? PointTransfer.transferPoint(field.getPoint()) : Double.parseDouble(field.getPoint());

                FieldStats stats = statsMap.computeIfAbsent(field.getFieldId(), k -> new FieldStats(field.getFieldId(), field.getFieldName()));

                List<Double> scoreList;
                Map<Integer, Double> universityScores;

                if (isTsa) {
                    scoreList = tsaFieldPoints.computeIfAbsent(field.getFieldId(), k -> new ArrayList<>());

                    if (stats.getTsaUniversityScores() == null) {
                        stats.setTsaUniversityScores(new HashMap<>());
                    }
                    universityScores = stats.getTsaUniversityScores();
                } else {
                    scoreList = thptFieldPoints.computeIfAbsent(field.getFieldId(), k -> new ArrayList<>());

                    if (stats.getThptUniversityScores() == null) {
                        stats.setThptUniversityScores(new HashMap<>());
                    }
                    universityScores = stats.getThptUniversityScores();
                }

                scoreList.add(score);
                universityScores.put(universityId, score);

                if (scoreList.size() == 1) {
                    // Nếu chỉ có 1 trường, hiển thị "Tên - Điểm" mà không lưu vào Map
                    stats.setReport(isTsa, universityName + " - " + score);
                } else {
                    // Nếu có nhiều trường, hiển thị min - max và LƯU vào Map
                    double minPoint = Collections.min(scoreList);
                    double maxPoint = Collections.max(scoreList);
                    stats.setReport(isTsa, minPoint + " - " + maxPoint);
                }
            }
        }
        for (FieldStats stats : statsMap.values()) {
            if (stats.getTsaUniversityScores() != null && stats.getTsaUniversityScores().size() <= 1) {
                stats.setTsaUniversityScores(null);
            }
            if (stats.getThptUniversityScores() != null && stats.getThptUniversityScores().size() <= 1) {
                stats.setThptUniversityScores(null);
            }
        }
    }

    public String getUniversityName(int universityCode) {
        University uni = universityRepository.findById(universityCode).orElse(null);
        return uni != null ? uni.getName() : "Unknown";
    }

//    @PostConstruct
//    public void scheduledUpdate() {
//        calculateAndStoreFieldStats();
//    }
}
