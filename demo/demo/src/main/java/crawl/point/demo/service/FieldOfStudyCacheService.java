package crawl.point.demo.service;

import com.github.benmanes.caffeine.cache.Cache;
import crawl.point.demo.entity.FieldOfStudy;
import crawl.point.demo.entity.PointByYear;
import crawl.point.demo.utils.Constant;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class FieldOfStudyCacheService {
    @Autowired
    PointService pointService;
    @Autowired
    Cache<String, Set<Integer>> complexTsaCache;
    @Autowired
    Cache<String, Set<Integer>> complexTHPTCache;
    @Getter
    private final Map<String, String> codeToFieldName = new HashMap<>();

    @PostConstruct
    public void init() {
        loadTsaCaches();
        loadTHPTCache();
    }
    private CompletableFuture<Void> loadTsaCaches() {
        return pointService.getAllPointByYear(Constant.TSA_NAME) // Chạy bất đồng bộ
                .thenAccept(points -> {
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
                            complexTsaCache.put(entry.getKey(), entry.getValue());
                        }
                    }
                });
    }


    private CompletableFuture<Void> loadTHPTCache() {
        return pointService.getAllPointByYear(Constant.THPT_NAME) // Chạy bất đồng bộ
                .thenAccept(points -> {
                    Map<String, Set<Integer>> fieldToUniversities = new HashMap<>();

                    for (PointByYear point : points) {
                        int universityId = point.getUniversityCode();
                        for (FieldOfStudy field : point.getFieldOfStudies()) {
                            fieldToUniversities
                                    .computeIfAbsent(field.getFieldId(), k -> new HashSet<>())
                                    .add(universityId);
                        }
                    }

                    // Đưa vào cache
                    for (Map.Entry<String, Set<Integer>> entry : fieldToUniversities.entrySet()) {
                        if (entry.getValue().size() >= 2) {
                            complexTHPTCache.put(entry.getKey(), entry.getValue());
                        }
                    }
                });
    }

}
