package com.interview.rtb.admission;

import com.interview.rtb.campaign.Campaign;
import com.interview.rtb.campaign.CampaignStatus;
import com.interview.rtb.campaign.Creative;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryCampaignAdmissionStoreTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-31T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void resetClearsBudgetAndFrequencyState() {
        InMemoryCampaignAdmissionStore store = new InMemoryCampaignAdmissionStore(clock);
        Campaign campaign = new Campaign(
                1L,
                "Demo",
                CampaignStatus.ACTIVE,
                "slot-home",
                "HK",
                Set.of("saas"),
                new BigDecimal("2.00"),
                new BigDecimal("2.00"),
                1,
                new Creative(10L, "https://example.com/ad.png", "https://example.com")
        );

        assertThat(store.tryAdmit("user-1", campaign)).isTrue();
        assertThat(store.tryAdmit("user-1", campaign)).isFalse();

        store.reset();

        assertThat(store.tryAdmit("user-1", campaign)).isTrue();
    }
}
