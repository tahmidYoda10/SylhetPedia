package com.sylhetpedia.backend.controller;

import com.sylhetpedia.backend.model.HomePageInfo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@CrossOrigin(origins = "*")

public class HomePageController {

    @GetMapping
    public HomePageInfo getHomepageInfo(){
        return new HomePageInfo(
                "http://localhost:8080/asset/SylhetPedia Logo.png",
                "http://localhost:8080/asset/SylhetPedia Background.jpg",
                "Sylhet at Your FingerTips!"
        );
    }
}
