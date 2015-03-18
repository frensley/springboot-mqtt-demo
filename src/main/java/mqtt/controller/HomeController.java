package mqtt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by sfrensley on 3/14/15.
 */
@Controller
public class HomeController {

    @RequestMapping({"/","home"})
    public String home() {
        return "home";
    }
}
