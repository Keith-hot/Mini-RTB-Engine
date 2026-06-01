package com.interview.rtb.bidding;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record BidRequest(
        @NotBlank String requestId,
        @NotBlank String userId,
        @NotBlank String placementId,
        @NotBlank String device,
        @NotBlank String country,
        @NotEmpty Set<String> userSegments
) {
}
