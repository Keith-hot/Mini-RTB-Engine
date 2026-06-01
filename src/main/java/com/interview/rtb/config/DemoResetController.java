package com.interview.rtb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/demo")
public class DemoResetController {

    private final DemoStateResetter demoStateResetter;
    private final boolean resetEnabled;

    public DemoResetController(DemoStateResetter demoStateResetter,
                               @Value("${app.demo.reset-enabled:false}") boolean resetEnabled) {
        this.demoStateResetter = demoStateResetter;
        this.resetEnabled = resetEnabled;
    }

    @GetMapping("/config")
    public DemoConfig config() {
        return new DemoConfig(resetEnabled);
    }

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset() {
        if (!resetEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        demoStateResetter.reset();
    }

    public record DemoConfig(boolean resetEnabled) {
    }
}
