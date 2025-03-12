package crawl.point.demo;

import crawl.point.demo.crawl.FetchNganhHoc;
import crawl.point.demo.crawl.FetchPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;

@SpringBootTest
@EnableAsync
class DemoApplicationTests {
    @Autowired
    FetchPoint fetchPoint;
    @Autowired
    FetchNganhHoc fetchNganhHoc;

    @Test
    void contextLoads() throws IOException {
//        fetchPoint.crawlPointUni();
        fetchNganhHoc.fetchNganhHoc();
    }

}
