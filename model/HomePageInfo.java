package com.sylhetpedia.backend.model;

public class HomePageInfo {
    private String logoUrl;
    private String backgroundUrl;
    private String welcomeText;

    public HomePageInfo(){}

    public HomePageInfo(String logoUrl, String backgroundUrl, String welcomeText){

        this.logoUrl = logoUrl;
        this.backgroundUrl = backgroundUrl;
        this.welcomeText = welcomeText;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public String getWelcomeText() {
        return welcomeText;
    }

    public void setWelcomeText(String welcomeText) {
        this.welcomeText = welcomeText;
    }

}
