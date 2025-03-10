package crawl.point.demo.Controller;

import crawl.point.demo.dto.FieldOfStudy;
import crawl.point.demo.dto.PointByYear;
import crawl.point.demo.dto.University;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UniversityController {
    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping({"/", "/home"})
    public String listUniversities(Model model) {
        List<University> universities = mongoTemplate.findAll(University.class);
        model.addAttribute("universities", universities);
        return "index";
    }

    @GetMapping("/points")
    public String getUniversityDetail(@RequestParam int id, Model model) {
        University university = mongoTemplate.findOne(
                Query.query(Criteria.where("id").is(id)), University.class
        );

        List<PointByYear> points = mongoTemplate.find(
                Query.query(Criteria.where("universityCode").is(id)), PointByYear.class
        );

        // Tạo danh sách năm học từ dữ liệu
        Set<Integer> years = points.stream()
                .map(PointByYear::getYear)
                .collect(Collectors.toCollection(TreeSet::new)); // Sắp xếp theo năm

        // Gom nhóm dữ liệu theo ngành
        Map<String, Map<Integer, String>> fieldData = new LinkedHashMap<>();
        for (PointByYear point : points) {
            for (FieldOfStudy field : point.getFieldOfStudies()) {
                fieldData.putIfAbsent(field.getFieldName(), new HashMap<>());
                fieldData.get(field.getFieldName()).put(point.getYear(), field.getPoint());
            }
        }

        model.addAttribute("university", university);
        model.addAttribute("years", years);
        model.addAttribute("fieldData", fieldData);
        return "university_detail";
    }
}
