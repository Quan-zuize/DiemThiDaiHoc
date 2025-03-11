package crawl.point.demo.service;

import com.github.benmanes.caffeine.cache.Cache;
import crawl.point.demo.Repository.PointByYearRepository;
import crawl.point.demo.entity.FieldOfStudy;
import crawl.point.demo.entity.PointByYear;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FieldOfStudyCacheService {

    private final PointByYearRepository pointByYearRepository;
    private final Cache<String, Set<Integer>> compareFieldCache;
    @Getter
    private final Map<String, String> codeToFieldName = new HashMap<>();

    public FieldOfStudyCacheService(PointByYearRepository pointByYearRepository,
                                    Cache<String, Set<Integer>> compareFieldCache,
                                    Cache<String, Integer> uniqueFieldCache) {
        this.pointByYearRepository = pointByYearRepository;
        this.compareFieldCache = compareFieldCache;
    }

    @PostConstruct
    public void init() {
        loadFieldCaches();
    }

    public void loadFieldCaches() {
        List<PointByYear> points = pointByYearRepository.findAll(); // Lấy toàn bộ dữ liệu ngành

        Map<String, Set<Integer>> fieldToUniversities = new HashMap<>();

        for (PointByYear point : points) {
            int universityId = point.getUniversityCode();
            for (FieldOfStudy field : point.getFieldOfStudies()) {
                fieldToUniversities
                        .computeIfAbsent(field.getFieldId(), k -> new HashSet<>())
                        .add(universityId);

                // Chỉ lưu tên ngành nếu chưa có
                codeToFieldName.putIfAbsent(field.getFieldId(), field.getFieldName());
            }
        }

        // Đưa vào cache
        for (Map.Entry<String, Set<Integer>> entry : fieldToUniversities.entrySet()) {
            if (entry.getValue().size() >= 2) {
                compareFieldCache.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
