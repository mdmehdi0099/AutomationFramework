package utils;

import com.gurock.qa.testrailManager.TestRailManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class BaseClass {


    private static final Logger log = LoggerFactory.getLogger(BaseClass.class);
    WebDriverWait explicitWaitListener;

    RemoteWebDriver driver = null;
    private Boolean isTestrail;

    public BaseClass() {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        explicitWaitListener = new WebDriverWait(driver, Duration.ofSeconds(100));
    }

    {
        isTestrail = Boolean.valueOf(ConfigReader.get("TestrailReadTestCase","false"));
        if (Boolean.TRUE.equals(isTestrail)) {
            TestRailManager.initializeTestCasesFromPlan();
        }
    }

    public WebDriver initializeDriver(String driverType, String projectName, String buildName, String testName) throws MalformedURLException {
        if (driver == null) {
            String driver1 = ConfigReader.get("TestDriver");
            String browser = ConfigReader.get("TestBrowserForUIAutomation");
            String username = ConfigReader.get("Username");
            String AccessKey = ConfigReader.get("AccessKey");
            String gridURL = "@hub.lambdatest.com/wd/hub";
            String tunnelName = ConfigReader.get("TunnelName");

            if (driverType.equalsIgnoreCase("Local")) {
                if (browser.equalsIgnoreCase("GoogleChrome")) {
                    driver = new ChromeDriver();
                } else if (browser.equalsIgnoreCase("MozilaFireFox")) {

                }
            } else {
                DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
                //set the capability
                driver = new RemoteWebDriver(new URL("http://" + username + ":" + AccessKey + gridURL), desiredCapabilities);
            }
            return driver;
        }
        return driver;
    }

    public void TestRailPassUpdate(String testCaseId,String message) throws IOException {
        TestRailManager.postResultToTestRail(testCaseId,TestRailManager.TEST_CASE_PASS_STATUS,message);
    }
    public void TestRailFailUpdate(String testCaseId,String message) throws IOException {
        TestRailManager.postResultToTestRail(testCaseId,TestRailManager.TEST_CASE_FAIL_STATUS,message);
    }


    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
            System.out.println("Driver quit and cleaned up.");
        }
    }


}
