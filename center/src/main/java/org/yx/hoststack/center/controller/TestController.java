package org.yx.hoststack.center.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yx.lib.utils.util.R;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/aaa")
    public R<?> test() {
        int a = 0;
        int b = 0;
        int c = a / b;
        return R.ok();
    }
}
