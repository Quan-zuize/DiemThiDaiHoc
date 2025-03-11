package crawl.point.demo.crawl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import crawl.point.demo.Repository.UniversityRepository;
import crawl.point.demo.entity.FieldOfStudy;
import crawl.point.demo.entity.PointByYear;
import crawl.point.demo.entity.University;
import crawl.point.demo.service.PointService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static crawl.point.demo.utils.Constant.*;

@Service
public class FetchNganhHoc {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    UniversityRepository universityRepository;
    @Autowired
    PointService pointService;

    private final OkHttpClient client = new OkHttpClient(); // OkHttp Client
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void fetchNganhHoc() {
        List<University> universities = universityRepository.findAll();

        for (University uni : universities) {
            boolean continueCrawl = true;
            int year = START_YEAR;
            while (continueCrawl) {
                String url = String.format(LIST_FIELD_OF_STUDY_API, uni.getId(), THPT_ID, year);
                System.out.println("Fetching: " + url);
                continueCrawl = crawlNganhVaDiemTHPT(url, year, uni.getId());
                year--;
            }
            System.out.println("Đã lấy xong dữ liệu từ trường: " + uni.getName());
        }
    }

    private boolean crawlNganhVaDiemTHPT(String url, int year, int code) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Lỗi khi gọi API: " + response);
                return false;
            }
            String json = response.body().string();
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode dataNode = rootNode.get("data");
            if (dataNode == null || !dataNode.isArray() || dataNode.isEmpty()) {
                return false;
            }

            List<FieldOfStudy> fieldOfStudies = new ArrayList<>();
            for (JsonNode node : dataNode) {
                FieldOfStudy fieldOfStudy = new FieldOfStudy();
                fieldOfStudy.setFieldId(node.get("code").asText());
                fieldOfStudy.setFieldName(node.get("name").asText());
                pointService.saveField(fieldOfStudy, NganhHoc);

                if(node.has("mark") && node.get("mark").asText().equals("0")) {
                    continue;
                }

                FieldOfStudy field = new FieldOfStudy();
                field.setFieldId(node.get("code").asText());
                field.setFieldName(node.get("name").asText());
                field.setPoint(node.get("mark").asText());
                if(!node.get("block").asText().isEmpty()) {
                    field.setSubjectCombine(node.get("block").asText());
                }
                if(!node.get("introtext").asText().isEmpty()){
                    field.setNote(node.get("introtext").asText());
                }
                fieldOfStudies.add(field);
            }

            if(fieldOfStudies.isEmpty()) {
                return false;
            }
            pointService.savePointByYear(new PointByYear(code, year, fieldOfStudies), THPT_NAME);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
