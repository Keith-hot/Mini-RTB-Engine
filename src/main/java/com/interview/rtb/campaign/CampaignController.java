package com.interview.rtb.campaign;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignListing campaignListing;

    public CampaignController(CampaignListing campaignListing) {
        this.campaignListing = campaignListing;
    }

    @GetMapping
    public List<Campaign> campaigns() {
        return campaignListing.activeCampaigns();
    }
}
