package crawl.point.demo.Controller;

import com.github.benmanes.caffeine.cache.Cache;
import crawl.point.demo.entity.PointByYear;
import crawl.point.demo.service.CompareService;
import crawl.point.demo.service.FieldOfStudyCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@RequestMapping("/compare")
@Controller
public class CompareController {
    @Autowired
    Cache<String, Set<Integer>> compareFieldCache;
    @Autowired
    FieldOfStudyCacheService fieldOfStudyCacheService;
    @Autowired
    CompareService compareService;

    @GetMapping
    public String comparePage(Model model) {
        Map<String, Set<Integer>> fields = compareFieldCache.asMap();
        Map<String, String> fieldNames = fieldOfStudyCacheService.getCodeToFieldName();

        model.addAttribute("fields", fields);
        model.addAttribute("fieldNames", fieldNames);
        return "compare";
    }

    @PostMapping("/result")
    public ResponseEntity<Map<String, Object>> compareField(@RequestParam("fieldCode") String fieldCode) {
        List<Integer> universityIds = new ArrayList<>(Objects.requireNonNull(compareFieldCache.getIfPresent(fieldCode)));

        if (universityIds.isEmpty()) {
            return ResponseEntity.ok(Map.of("error", "Không có trường nào có ngành này để so sánh."));
        }

        // Lấy danh sách điểm ngành theo năm
        List<PointByYear> points = compareService.getPointByYears(fieldCode, universityIds);

        // Trả về JSON
        return ResponseEntity.ok(Map.of(
                "fieldName", fieldOfStudyCacheService.getCodeToFieldName().get(fieldCode),
                "universityPoints", compareService.getUniversityPoints(fieldCode, universityIds, points),
                "years", compareService.getAndSortYears(points)
        ));
    }
}
