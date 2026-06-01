package com.interview.rtb.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemoResetControllerTest {

    @Test
    void reportsWhetherDemoResetIsEnabled() {
        RecordingDemoStateResetter resetter = new RecordingDemoStateResetter();
        DemoResetController controller = new DemoResetController(resetter, true);

        assertThat(controller.config().resetEnabled()).isTrue();
    }

    @Test
    void hidesResetEndpointWhenDisabled() {
        RecordingDemoStateResetter resetter = new RecordingDemoStateResetter();
        DemoResetController controller = new DemoResetController(resetter, false);

        assertThatThrownBy(controller::reset)
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resetter.resetCalled).isFalse();
    }

    @Test
    void allowsResetWhenEnabled() {
        RecordingDemoStateResetter resetter = new RecordingDemoStateResetter();
        DemoResetController controller = new DemoResetController(resetter, true);

        controller.reset();

        assertThat(resetter.resetCalled).isTrue();
    }

    private static class RecordingDemoStateResetter implements DemoStateResetter {
        private boolean resetCalled;

        @Override
        public void reset() {
            resetCalled = true;
        }
    }
}
