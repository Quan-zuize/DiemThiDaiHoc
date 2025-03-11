package crawl.point.demo.service;

import crawl.point.demo.Repository.PointByYearRepository;
import crawl.point.demo.Repository.UniversityRepository;
import crawl.point.demo.entity.FieldOfStudy;
import crawl.point.demo.entity.PointByYear;
import crawl.point.demo.entity.University;
import crawl.point.demo.utils.PointTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CompareService {
    @Autowired
    PointByYearRepository pointByYearRepository;
    @Autowired
    UniversityRepository universityRepository;

    public Map<String, Map<Integer, String>> getUniversityPoints(String fieldCode, List<Integer> universityIds, List<PointByYear> points) {
        // Lấy danh sách trường (ID -> Name)
        Map<Integer, String> universityMap = universityRepository.findByIdIn(universityIds).stream()
                .collect(Collectors.toMap(University::getId, University::getName));

        // Gom dữ liệu theo tên trường thay vì ID
        // Key: Tên trường
        return points.stream()
                .collect(Collectors.groupingBy(
                        p -> universityMap.get(p.getUniversityCode()), // Key: Tên trường
                        Collectors.toMap(
                                PointByYear::getYear,
                                p -> p.getFieldOfStudies().stream()
                                        .filter(f -> f.getCode().equals(fieldCode))
                                        .map(f -> PointTransfer.transferPoint(f.getPoint()) + "")
                                        .findFirst()
                                        .orElse("-"))));
    }

    public List<PointByYear> getPointByYears(String fieldCode, List<Integer> universityIds) {
        return pointByYearRepository.findByUniversityCodeInAndFieldCode(universityIds, fieldCode);
    }

    public List<Integer> getAndSortYears(List<PointByYear> points) {
        return points.stream()
                .map(PointByYear::getYear)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
