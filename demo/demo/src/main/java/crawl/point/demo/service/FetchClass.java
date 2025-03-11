package crawl.point.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import crawl.point.demo.entity.University;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static crawl.point.demo.utils.Constant.LIST_UNIVERSITY_API;

@Service
public class FetchClass {
    private final OkHttpClient client = new OkHttpClient(); // OkHttp Client
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FetchClass() {
    }

    public List<University> fetchSchoolList() throws IOException {
        Request request = new Request.Builder().url(LIST_UNIVERSITY_API).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Lỗi khi gọi API: " + response);
                return null;
            }
            String json = response.body().string();

            // Lấy phần "data" từ JSON
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode dataNode = rootNode.get("data");

            return Arrays.asList(objectMapper.treeToValue(dataNode, University[].class));
        }
    }
}
