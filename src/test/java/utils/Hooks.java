package utils;

import com.gurock.qa.testrailManager.TestRailManager;
import com.gurock.testrail.APIException;
import context.SharedContext;
import exceptions.config.ConfigException;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.Reporter;
import stepDefinitions.StepDefinition;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

public class Hooks {
    private static final Logger log = LoggerFactory.getLogger(Hooks.class);
    private ByteArrayOutputStream consoleOutput;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private final SharedContext context;
    private final BaseClass base;
    WebDriver driver;
    {
        try {
            base = new BaseClass();
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            ConfigReader.load();
            System.out.println("Config Loaded Successfully");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Hooks(SharedContext context) {
        this.context = context;
    }

    @Before
    public void beforeScenario(Scenario scenario) throws ConfigException {
        consoleOutput = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        PrintStream combinedStream = new PrintStream(consoleOutput, true);
        System.setOut(combinedStream);
        System.setErr(combinedStream);
        Collection<String> tags = scenario.getSourceTagNames();
        //read the global.properties file and then execute accordingly
        Boolean isTestrail = Boolean.valueOf(ConfigReader.get("TestrailReadTestCase"));
        Boolean isUpdateTestRail = Boolean.valueOf(ConfigReader.get("UpdateTestRail"));
        Boolean UpdateLambda = Boolean.valueOf(ConfigReader.get("UpdateLambda"));
        if (Boolean.TRUE.equals(isTestrail)) {
            // Initialize TestRail test cases once on class load
            TestRailManager.initializeTestCasesFromPlan();
        }
        context.setTestrail(isTestrail);
        context.setUpdateTestrail(isUpdateTestRail);
        context.setUpdateLambda(UpdateLambda);
        String testCaseId = null;
        for (String tag : tags) {
            if (tag.startsWith("@C")) {
                testCaseId = tag.replace("@C", "").trim();
                context.setTestCaseId(testCaseId);
                log.info("testCaseId:----------:" + testCaseId);
                break;
            }
        }
        context.setTestName(scenario.getName());
        log.info("Test Name:" + scenario.getName());
        try {
            String projectName = "";
            if (context.isTestrail()) {
                String projectID = ConfigReader.get("projectID");
                projectName = getProjectName(projectID);
                context.setProjectName(projectName);
                log.info("projectID from testrail: " + projectID);
                log.info("projectName from testrail: " + projectName);
            } else {
                projectName = ConfigReader.get("projectName");
                context.setProjectName(projectName);
                log.info("projectName from properties file: " + projectName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //setBuildName
        String buildName = "";
        if (context.isTestrail()) {
            /*
            String buildID = ConfigReader.get("TESTPLANID");
            //System.out.println("the value of build id is : " + buildID);
            buildName = getBuildName(buildID);
            log.info("The value of build id is:"+buildID);
            //System.out.println("the value of buildName is : " + buildName);
            context.setBuildName(buildName);
            log.info("TESTPLANID "+buildID);
            log.info("buildName from testrail: "+buildName);
            */
            buildName = ConfigReader.get("BuildName");
            context.setBuildName(buildName);
            log.info("buildName from properties file : " + buildName);
        } else {
            buildName = ConfigReader.get("BuildName");
            context.setBuildName(buildName);
            log.info("buildName from properties file : " + buildName);
        }
        if (context.isTestrail()) {
            // Optional: fetch browser/build/etc. from TestRail Plan
            if (testCaseId != null) {
                Map<String, Map<String, String>> testCasesFromPlan = TestRailManager.getTestCasesFromPlan();
                if (testCasesFromPlan.containsKey(testCaseId)) {
                    Map<String, String> caseDetails = testCasesFromPlan.get(testCaseId);
                    context.setBrowser(caseDetails.getOrDefault("browser", "chrome"));
                }
            }
        } else {
            String browser = ConfigReader.get("browser");
            context.setBrowser(browser);
        }
    }
    @After
    public void tearDown(Scenario scenario) {
        WebDriver driver = context.getDriver();
        log.info("=================================================");
        log.info("After Scenario Started");
        log.info("Scenario Name : {}", scenario.getName());
        log.info("=================================================");
        String testCaseId = context.getTestCaseId();
        String status =scenario.isFailed()
                        ? "failed"
                        : "passed";
        Throwable failure =StepDefinition.getLastError();
        String consoleLogs =consoleOutput != null
                        ? consoleOutput.toString()
                        : "";
        StringBuilder commentBuilder =new StringBuilder();
        commentBuilder.append(status.toUpperCase())
                .append("\nScenario: ")
                .append(scenario.getName())
                .append("\nTags: ")
                .append(scenario.getSourceTagNames());
        if (failure != null) {
            commentBuilder.append("\n\nException:\n")
                    .append(
                            StepDefinition
                                    .getStackTrace(failure));
        }
        if (!consoleLogs.isEmpty()) {
            commentBuilder.append("\n\nConsole Output:\n").append(consoleLogs);
        }
        String comment =commentBuilder.toString();
        try {
            // =================================================
            // Attach failure logs
            // =================================================
            if (scenario.isFailed()
                    && !consoleLogs.isEmpty()) {
                scenario.attach(
                        consoleLogs,
                        "text/plain",
                        "Failure Console Output");
            }
            // =================================================
            // LambdaTest Status Update
            // =================================================
            String executionType =ConfigReader.get("ExecutionType");
            if ("Remote".equalsIgnoreCase(executionType)
                    && context.isUpdateLambda()) {
                try {
                    if (driver instanceof JavascriptExecutor) {
                        ((JavascriptExecutor) driver)
                                .executeScript("lambda-status="+ status);
                        log.info("LambdaTest status updated : {}",status);
                    }
                } catch (Exception e) {
                    log.error("Failed to update LambdaTest status",e);
                }
            }
            // =================================================
            // TestRail Update
            // =================================================
            if (context.isTestrail()&& context.isUpdateTestrail()&& testCaseId != null&& !testCaseId.isEmpty()) {
                try {
                    if ("passed".equalsIgnoreCase(status)) {
                        log.info("Updating TestRail PASS");
                        TestRailPassUpdate(testCaseId,comment);
                    } else {
                        log.info("Updating TestRail FAIL");
                        TestRailFailUpdate(testCaseId,comment);
                    }
                } catch (Exception e) {
                    log.error("Failed to update TestRail",e);
                }
            }
        } catch (Exception e) {
            log.error("Unexpected error in @After hook",e);
        } finally {
            // =================================================
            // ALWAYS restore console
            // =================================================
            try {
                if (originalOut != null) {
                    System.setOut(originalOut);
                }
                if (originalErr != null) {
                    System.setErr(originalErr);
                }
            } catch (Exception e) {
                log.error("Failed restoring console streams",e);
            }
            // =================================================
            // Driver Cleanup
            // =================================================
            try {
                if (driver != null) {
                    driver.quit();
                    context.setDriver(null);
                    log.info("Driver closed for scenario: {}",scenario.getName());
                }
            } catch (Exception e) {
                log.error("Error while quitting driver",e);
            }
            // =================================================
            // Clear ThreadLocal Error
            // =================================================
            try {
                StepDefinition.clearLastError();
            } catch (Exception e) {
                log.error("Failed clearing ThreadLocal error",e);
            }
            log.info("=================================================");
            log.info("After Scenario Completed");
            log.info("=================================================");
        }
    }
    public void TestRailPassUpdate(String testCaseId, String message) {
        try {
            int status = TestRailManager.TEST_CASE_PASS_STATUS;
            TestRailManager.postResultToTestRail(testCaseId, status, message);
        } catch (IOException e) {
            log.info("Error updating TestRail (Pass): " + e.getMessage());
        }
    }

    public void TestRailFailUpdate(String testCaseId, String message) {
        try {
            int status = TestRailManager.TEST_CASE_FAIL_STATUS;
            TestRailManager.postResultToTestRail(testCaseId, status, message);
        } catch (IOException e) {
            log.info("Error updating TestRail (Fail): " + e.getMessage());
        }
    }


    public static String getProjectName(String projectID) throws IOException, ConfigException {
        String projectName = "";
        String URL = ConfigReader.get("TestRailAPIProjectNameURL", "testRail") + projectID;
        String username1 = ConfigReader.get("TestRailUsername", "TestRailUsername");
        String password = ConfigReader.get("TestRailPassword", "TestRailPassword");
        //sending the get request
        Response response = RestAssured.given().auth()
                .preemptive().basic(username1, password)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .when()
                .get(URL);
        if (response.getStatusCode() == 200) {
            projectName = response.jsonPath().getString("name");
        }
        return projectName;
    }

    public static String getBuildName(String buildID) throws IOException, ConfigException {
        String buildName = "";
        String URL = ConfigReader.get("TestRailAPIProjectNameURL", "TestRailAPIProjectNameURL") + buildID;
        String username1 = ConfigReader.get("TestRailUsername", "TestRailUsername");
        String password = ConfigReader.get("TestRailPassword", "TestRailPassword");
        //sending the get request
        Response response = RestAssured.given().auth()
                .preemptive().basic(username1, password)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .when()
                .get(URL);
        if (response.getStatusCode() == 200) {
            buildName = response.jsonPath().getString("name");
        }
        return buildName;
    }


}
