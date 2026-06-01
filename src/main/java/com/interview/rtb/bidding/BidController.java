package com.interview.rtb.bidding;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BidController {

    private final BidDecisionService bidDecisionService;

    public BidController(BidDecisionService bidDecisionService) {
        this.bidDecisionService = bidDecisionService;
    }

    @PostMapping("/bid")
    public BidResponse bid(@Valid @RequestBody BidRequest request) {
        return bidDecisionService.decide(request);
    }
}
