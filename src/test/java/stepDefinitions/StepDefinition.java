package stepDefinitions;
//
//import context.SharedContext;
//import hooks.Hooks;
//import io.cucumber.java.Scenario;
//import io.cucumber.java.en.Given;
//import io.cucumber.java.en.Then;
//import io.cucumber.java.en.When;
//import org.openqa.selenium.WebDriver;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import utils.BaseClass;
//import utils.ConfigReader;
//import java.net.MalformedURLException;
//
public class StepDefinition {
//    BaseClass baseClass = new BaseClass();
//    private final SharedContext context;
//    Scenario scn;
//    String driver1;
//    WebDriver driver;
//    String status = "failed";
//    String projectName = "";
//    String buildName = "";
//    String username, password, userCredential;
//    private String testCaseId = "224";
//    String titlePage;
//    String message = "";
//    String UpdateTestRail = "";
//    int testScenario = 0;
//    int testCase = 0;
//
//
//    public StepDefinition(SharedContext context, BaseClass baseClass) {
//        this.context = context;
//        this.baseClass = baseClass;
//    }
//
//    private static final Logger log = LoggerFactory.getLogger(StepDefinition.class);
//
//
//    @Given("The user navigates to {string}")
//    public void the_user_navigates_to(String url) throws MalformedURLException {
//        String testName= context.getTestName();
//        driver1= ConfigReader.get("TestDriver");
//        projectName=ConfigReader.get("ProjectName");
//        buildName=context.getBuildName();
//        projectName=context.getProjectName();
//        driver= baseClass.initializeDriver(driver1,projectName,buildName,testName);
//        context.setDriver(driver);
//        context.initializePageObject(driver);
//        driver.get(url);
//        driver.manage().window().maximize();
//    }
//
//    @When("The user enters username {string} and password {string}")
//    public void the_user_enters_username_and_password(String string, String string2) {
//
//    }
//
//    @When("The user clicks on the login button")
//    public void the_user_clicks_on_the_login_button() {
//
//    }
//
//    @Then("The user should be redirected to the dashboard")
//    public void the_user_should_be_redirected_to_the_dashboard() {
//
//    }
//
//
}
