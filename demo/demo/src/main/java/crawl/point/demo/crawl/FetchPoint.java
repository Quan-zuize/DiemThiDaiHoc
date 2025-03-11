package crawl.point.demo.crawl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import crawl.point.demo.entity.FieldOfStudy;
import crawl.point.demo.entity.PointByYear;
import crawl.point.demo.entity.University;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static crawl.point.demo.utils.Constant.*;

@Service
public class FetchPoint {
    @Autowired
    private MongoTemplate mongoTemplate;

    private final OkHttpClient client = new OkHttpClient(); // OkHttp Client
    private final ObjectMapper objectMapper = new ObjectMapper();

    FetchClass fetchClass = new FetchClass();

    public void crawlPointUni() throws IOException {
        List<University> universities = fetchClass.fetchSchoolList();

        if (universities == null || universities.isEmpty()) {
            System.out.println("Không lấy được danh sách trường.");
            return;
        }

        for (University uni : universities) {
            boolean continueCrawl = true;
            int year = START_YEAR;
            while (continueCrawl) {
                String url = String.format(LIST_FIELD_OF_STUDY_API, uni.getId(), TSA_ID, year);
                System.out.println("Fetching: " + url);
                continueCrawl = crawlDiem(url, year, uni.getId());
                year--;
            }
            System.out.println("Đã lấy xong dữ liệu từ trường: " + uni.getName());
        }
    }

    public boolean crawlDiem(String url, int year, int code) throws IOException {
        // Crawl điểm chuẩn từ URL

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
           mongoTemplate.save(new PointByYear(code, year, fieldOfStudies), TSA_NAME);
            return true;
        }
    }
}
