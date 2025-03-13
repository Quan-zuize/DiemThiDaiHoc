package crawl.point.demo.Controller;

import crawl.point.demo.entity.FieldStats;
import crawl.point.demo.entity.UniversityScoreDTO;
import crawl.point.demo.service.FieldStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/fields")
@Controller
public class FieldController {
    @Autowired
    private FieldStatsService fieldStatsService;
    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping
    public String listFields(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "15") int size,
                             @RequestParam(required = false) String keyword,
                             Model model) {
        Page<FieldStats> fieldStatsPage = fieldStatsService.getPagedFieldStats(page, size, keyword);

        model.addAttribute("totalItems", fieldStatsPage.getTotalElements());
        model.addAttribute("fieldStatsPage", fieldStatsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", fieldStatsPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("baseUrl", "/fields"); // Base URL cho phân trang
        return "fields";
    }

    @GetMapping("/detail/{id}")
    public String getFieldDetail(@PathVariable String id, @RequestParam String type, Model model) {
        FieldStats stats = mongoTemplate.findById(id, FieldStats.class);
        if (stats == null) {
            return "redirect:/fields"; // Nếu không có dữ liệu, quay lại trang fields
        }

        // Chọn map scores theo type
        Map<Integer, Double> universityScores = "tsa".equals(type) ? stats.getTsaUniversityScores() : stats.getThptUniversityScores();

        List<UniversityScoreDTO> universityList = new ArrayList<>();
        if (universityScores == null || universityScores.isEmpty()) {
            String report;
            if("tsa".equals(type)) {
                report = stats.getTsaReport();
            } else {
                report = stats.getTsaReport();
            }
            universityList.add(new UniversityScoreDTO(report.split(":")[0], Double.parseDouble(report.split(":")[1])));
        }else{
            // Lấy danh sách các trường
            universityList = universityScores.entrySet().stream()
                    .map(entry -> new UniversityScoreDTO(fieldStatsService.getUniversityName(entry.getKey()), entry.getValue()))
                    .sorted(Comparator.comparingDouble(UniversityScoreDTO::getScore).reversed()) // Sắp xếp giảm dần
                    .collect(Collectors.toList());
        }

        model.addAttribute("fieldName", stats.getFieldName());
        model.addAttribute("universityList", universityList);
        model.addAttribute("type", "tsa".equals(type) ? "TSA" : "THPT");

        return "field_detail";
    }
}