package crawl.point.demo.Controller;

import crawl.point.demo.entity.FieldOfStudy;
import crawl.point.demo.entity.University;
import crawl.point.demo.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/fields")
@Controller
public class FieldController {
    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping
    public String listFields(Model model) {
        List<FieldOfStudy> universities = mongoTemplate.findAll(FieldOfStudy.class, Constant.NganhHoc);
        model.addAttribute("fields", universities);
        return "fields";
    }
}
