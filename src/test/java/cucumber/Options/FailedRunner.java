package cucumber.Options;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "@target/failed_scenarios.txt",
        glue = {"stepDefinitions", "utils"},
        plugin = {
                "pretty",
                "html:target/cucumber-retry.html"
        }
)
public class FailedRunner extends AbstractTestNGCucumberTests {
}