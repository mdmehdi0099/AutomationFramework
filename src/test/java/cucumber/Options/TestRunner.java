package cucumber.Options;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import utils.RetryListener;

//@Listeners({utils.RetryListener.class})
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"stepDefinitions", "utils"},
        tags = "@C46",
        plugin = {
                "pretty",
                "summary",
                "html:target/cucumber-reports.html",
                "json:target/cucumber.json"
                //"rerun:target/failed_scenarios.txt"
        },
        monochrome = false
)
public class TestRunner extends AbstractTestNGCucumberTests {

    /*
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
    */
}
