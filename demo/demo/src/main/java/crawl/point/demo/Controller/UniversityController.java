package crawl.point.demo.Controller;

import crawl.point.demo.entity.FieldOfStudy;
import crawl.point.demo.entity.PointByYear;
import crawl.point.demo.entity.University;
import crawl.point.demo.utils.Constant;
import crawl.point.demo.utils.PointTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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

        // Tạo tiêu chí lọc theo năm
        Criteria yearCriteria = Criteria.where("year").gte(2021).lte(2024);
        Criteria year2Criteria = Criteria.where("year").gte(2022).lte(2024);

        // Chạy song song lấy TSA & THPT
        CompletableFuture<List<PointByYear>> tsaFuture = CompletableFuture.supplyAsync(() ->
                mongoTemplate.find(
                        Query.query(Criteria.where("universityCode").is(id).andOperator(year2Criteria)),
                        PointByYear.class, Constant.TSA_NAME
                )
        );

        CompletableFuture<List<PointByYear>> thptFuture = CompletableFuture.supplyAsync(() ->
                mongoTemplate.find(
                        Query.query(Criteria.where("universityCode").is(id).andOperator(yearCriteria)),
                        PointByYear.class, Constant.THPT_NAME
                )
        );

        // Chờ cả hai hoàn thành
        List<PointByYear> tsaPoints = tsaFuture.join();
        List<PointByYear> thptPoints = thptFuture.join();

        // Tạo danh sách năm học (TreeSet để tự động sắp xếp)
        Set<Integer> years = new TreeSet<>();
        Set<Integer> years2 = new TreeSet<>();
        tsaPoints.forEach(p -> years2.add(p.getYear()));
        thptPoints.forEach(p -> years.add(p.getYear()));

        // Gom nhóm dữ liệu theo ngành (TSA & THPT)
        CompletableFuture<Map<String, Map<Integer, Double>>> tsaDataFuture = CompletableFuture.supplyAsync(() -> processPoints(tsaPoints, true));
        CompletableFuture<Map<String, Map<Integer, Double>>> thptDataFuture = CompletableFuture.supplyAsync(() -> processPoints(thptPoints, false));

        // Chờ xử lý dữ liệu xong
        Map<String, Map<Integer, Double>> tsaFieldData = tsaDataFuture.join();
        Map<String, Map<Integer, Double>> thptFieldData = thptDataFuture.join();
        Map<String, String> fieldIdMap = new HashMap<>();
        for (PointByYear point : thptPoints) {
            for (FieldOfStudy field : point.getFieldOfStudies()) {
                fieldIdMap.put(field.getFieldName(), field.getFieldId());
            }
        }

        model.addAttribute("university", university);
        model.addAttribute("years", years);
        model.addAttribute("years2", years2);
        model.addAttribute("tsaFieldData", tsaFieldData);
        model.addAttribute("thptFieldData", thptFieldData);
        model.addAttribute("fieldIdMap", fieldIdMap);

        return "university_detail";
    }

    // Hàm xử lý điểm chung cho cả TSA & THPT
    private Map<String, Map<Integer, Double>> processPoints(List<PointByYear> points, boolean isTsa) {
        Map<String, Map<Integer, Double>> fieldData = new LinkedHashMap<>();

        for (PointByYear point : points) {
            for (FieldOfStudy field : point.getFieldOfStudies()) {
                fieldData.putIfAbsent(field.getFieldName(), new HashMap<>());
                double score = isTsa ? PointTransfer.transferPoint(field.getPoint()) : Double.parseDouble(field.getPoint());
                fieldData.get(field.getFieldName()).put(point.getYear(), score);
            }
        }
        return fieldData;
    }
}
