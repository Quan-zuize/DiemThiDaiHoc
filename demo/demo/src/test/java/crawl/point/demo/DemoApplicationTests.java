package crawl.point.demo;

import crawl.point.demo.service.FetchPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class DemoApplicationTests {
    @Autowired
    FetchPoint fetchPoint;

    @Test
    void contextLoads() throws IOException {
        fetchPoint.crawlPointUni();
    }

}
