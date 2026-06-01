package com.interview.rtb.campaign;

import com.interview.rtb.bidding.BidRequest;

import java.util.List;

public interface CampaignReadModel {
    List<Campaign> candidatesFor(BidRequest request);
}
