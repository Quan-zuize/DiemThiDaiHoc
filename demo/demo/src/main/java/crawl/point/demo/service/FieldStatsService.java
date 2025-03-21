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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

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
                    stats.setReport(isTsa, universityName + ": " + score);
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

    public void normalizeFieldData() {
        // Bước 1: Tạo Map chuẩn hóa fieldName -> fieldId + fieldName mới nhất (2024) cho từng universityCode
        Map<Integer, Map<String, FieldOfStudy>> universityFieldMap = new HashMap<>();

        List<PointByYear> latestPoints = mongoTemplate.find(
                Query.query(Criteria.where("year").is(2024)), PointByYear.class, Constant.TSA_NAME
        );

        for (PointByYear point : latestPoints) {
            int universityCode = point.getUniversityCode();
            universityFieldMap.putIfAbsent(universityCode, new HashMap<>());

            for (FieldOfStudy field : point.getFieldOfStudies()) {
                String normalizedFieldName = normalizeFieldName(field.getFieldName());
                universityFieldMap.get(universityCode).put(normalizedFieldName, field);
            }
        }

        // Bước 2: Cập nhật dữ liệu cũ (2021, 2022)
        List<PointByYear> oldPoints = mongoTemplate.find(
                Query.query(Criteria.where("year").in(2022, 2023)), PointByYear.class, Constant.TSA_NAME
        );

        for (PointByYear point : oldPoints) {
            int universityCode = point.getUniversityCode();
            Map<String, FieldOfStudy> fieldMap = universityFieldMap.get(universityCode);
            if (fieldMap == null) continue; // Nếu trường này không có dữ liệu năm 2024 thì bỏ qua

            boolean updated = false;
            for (FieldOfStudy field : point.getFieldOfStudies()) {
                String normalizedFieldName = normalizeFieldName(field.getFieldName());
                FieldOfStudy latestField = fieldMap.get(normalizedFieldName);

                if (latestField != null && (!latestField.getFieldId().equals(field.getFieldId())
                        || !latestField.getFieldName().equals(field.getFieldName()))) {

                    field.setFieldId(latestField.getFieldId());
                    field.setFieldName(latestField.getFieldName());
                    updated = true;
                }
            }

            if (updated) {
                // Xóa bản ghi cũ
                mongoTemplate.remove(Query.query(Criteria.where("_id").is(point.getId())), Constant.TSA_NAME);

                // Chèn bản ghi mới với fieldId & fieldName chuẩn hóa
                mongoTemplate.insert(point, Constant.TSA_NAME);
            }
        }

        System.out.println("Hoàn tất chuẩn hóa fieldId & fieldName trên bảng TSA_NAME!");
    }

    // Chuẩn hóa fieldName để so sánh chính xác
    private static String normalizeFieldName(String fieldName) {
        if (fieldName == null) return "";

        fieldName = fieldName.toLowerCase();

        // Thay thế từ viết tắt
        fieldName = fieldName.replaceAll("\\bct\\b", "chương trình");
        fieldName = fieldName.replaceAll("\\bcntt\\b", "công nghệ thông tin");
        fieldName = fieldName.replaceAll("\\bgd\\b", "giáo dục");

        // Xóa cụm từ không cần thiết
        fieldName = fieldName.replaceAll("\\bhợp tác với\\b", "");
        fieldName = fieldName.replaceAll("\\bliên kết với\\b", "");
        fieldName = fieldName.replaceAll("\\bchất lượng cao\\b", "");
        fieldName = fieldName.replaceAll("\\bngành\\b", "");
        fieldName = fieldName.replaceAll("\\bcử nhân\\b", "");

        fieldName = removeVietnameseAccents(fieldName);
        fieldName = fieldName.replaceAll("đ", "d"); // Thay thế ký tự đ
        fieldName = fieldName.replaceAll("y", "i"); // Thay thế ký tự đ
        fieldName = fieldName.replaceAll("[^a-z0-9]", ""); // Loại bỏ ký tự đặc biệt
        fieldName = fieldName.trim().replaceAll("\\s+", " "); // Xóa khoảng trắng thừa

        return fieldName.trim();
    }

    public static String removeVietnameseAccents(String str) {
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    @PostConstruct
    public void scheduledUpdate() {
        calculateAndStoreFieldStats();
//        normalizeFieldData();
    }
}
