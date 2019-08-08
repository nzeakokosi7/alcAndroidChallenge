package com.varscon.travelmantics;

public class TravelModel {
    public String imageUrl;
    public String dealTitle;
    public String dealPrice;
    public String dealDescription;

    public TravelModel() {
    }

    public TravelModel(String imageUrl, String dealTitle, String dealPrice, String dealDescription) {
        this.imageUrl = imageUrl;
        this.dealTitle = dealTitle;
        this.dealPrice = dealPrice;
        this.dealDescription = dealDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDealTitle() {
        return dealTitle;
    }

    public void setDealTitle(String dealTitlt) {
        this.dealTitle = dealTitlt;
    }

    public String getDealPrice() {
        return dealPrice;
    }

    public void setDealPrice(String dealPrice) {
        this.dealPrice = dealPrice;
    }

    public String getDealDescription() {
        return dealDescription;
    }

    public void setDealDescription(String dealDescription) {
        this.dealDescription = dealDescription;
    }
}
