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

    public Hooks(SharedContext context) {
        this.context = context;
    }

    @BeforeAll
    public static void beforeAll() throws ConfigException {
        ConfigReader.load(); //load once }
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
        log.info("TestrailReadTestCase");
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
    public void tearDown(Scenario scenario) throws IOException, ConfigException {
        driver = context.getDriver();
        //String ExecutionType=this.getglobalValue("TestDriver");
        System.out.println("After scenario is starting............................");
        String testCaseId = context.getTestCaseId();
        String status = "passed";
        String comment = "PASSED";
        String consoleLogs = consoleOutput.toString();
        Throwable failure = StepDefinition.getLastError();
        try {
            if (scenario.isFailed()) {
                status = "failed";
                comment = "FAILED\n" + "Scenario: " + scenario.getName() + "\n" + "Tags: " + scenario.getSourceTagNames() + "\n\n" + "Console Output:\n" + consoleOutput.toString();
                if (failure != null) {
                    comment += "Exception:\n" + StepDefinition.getStackTrace(failure) + "\n";
                }
                comment += "Console Output:\n" + consoleLogs;
                //comment = "FAILED: " + scenario.getStatus().toString() +(scenario.getStatus().name().equals("FAILED") ? scenario.getName() : ""); }
                if (scenario.isFailed()) {
                    scenario.attach(consoleOutput.toString(), "text/plain", "Failure Console Output");
                }
            }
            System.out.println("after scenario.isFailed ....");
            comment += "\nConsole Output:\n" + consoleOutput.toString();
            comment += "\nScenario: " + scenario.getName();
            comment += "\nTags: " + scenario.getSourceTagNames();
            comment += "Console Output:\n" + consoleLogs;
            String ExecutionType = ConfigReader.get("TestDriver");
            log.info("The value of ExecutionType is : " + ExecutionType);
            boolean UpdateLambda = Boolean.parseBoolean(ConfigReader.get("UpdateLambda"));
            log.info("The value of context.isUpdateLambda : " + context.isUpdateLambda());
            if ("Remote".equalsIgnoreCase(ExecutionType) && context.isUpdateLambda()) {
                System.out.println("123execution............");
                if (driver instanceof JavascriptExecutor) {
                    ((JavascriptExecutor) driver).executeScript("lambda-status=" + status);
                    log.info("lambdatest execution is called.....");
                    System.out.println("execution............");
                }
            }
            System.setOut(originalOut);
            System.setErr(originalErr);
            //execute the testrail only if the value passed in global.properties is true
            if (context.isTestrail() && context.isUpdateTestrail()) {
                if (testCaseId != null && !testCaseId.isEmpty()) {
                    if (status.equals("passed")) {
                        log.info("TestRailPassUpdate");
                        TestRailPassUpdate(testCaseId, comment);
                    } else {
                        log.info("TestRailFailUpdate");
                        TestRailFailUpdate(testCaseId, comment);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during afterScenario logic: " + e.getMessage());
            e.printStackTrace();
        } finally {
            driver = context.getDriver();
            if (driver != null) {
                driver.quit();
                context.setDriver(null);
                log.info("Driver closed for scenario: {}", scenario.getName());
            }
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
