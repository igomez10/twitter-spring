package com.ignacio.twitter.gherkin;

import io.cucumber.core.cli.Main;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CucumberTest {

    @Test
    void runFeatures() {
        byte exitStatus = Main.run(new String[]{
                "--plugin", "pretty",
                "--glue", "com.ignacio.twitter.gherkin",
                "classpath:features"
        }, Thread.currentThread().getContextClassLoader());

        assertThat(exitStatus).isZero();
    }
}
