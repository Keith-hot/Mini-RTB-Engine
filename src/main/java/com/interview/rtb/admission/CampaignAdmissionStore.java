package com.interview.rtb.admission;

import com.interview.rtb.campaign.Campaign;

public interface CampaignAdmissionStore {
    boolean tryAdmit(String userId, Campaign campaign);
}
