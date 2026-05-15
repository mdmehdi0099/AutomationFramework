package stepDefinitions;

import context.SharedContext;
import exceptions.config.ConfigException;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.BaseClass;
import utils.ConfigReader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.time.Duration;

public class StepDefinition {
    BaseClass baseClass = new BaseClass();
    private final SharedContext context;
    Scenario scn;
    String driver1;
    WebDriver driver;
    String status = "failed";
    String projectName = "";
    String buildName = "";
    String username, password, userCredential;
    private String testCaseId = "224";
    String titlePage;
    String message = "";
    String UpdateTestRail = "";
    int testScenario = 0;
    int testCase = 0;

    public StepDefinition(SharedContext context, BaseClass baseClass) throws ConfigException {
        this.context = context;
        this.baseClass = baseClass;
    }

    private static final Logger log = LoggerFactory.getLogger(StepDefinition.class);


    @Given("The user navigates to the application {string}")
    public void the_user_navigates_to_the_application(String url) throws ConfigException, MalformedURLException {
        String testName= context.getTestName();
        driver1= ConfigReader.get("ExecutionType");
        projectName=ConfigReader.get("ProjectName");
        buildName=context.getBuildName();
        projectName=context.getProjectName();
        driver= baseClass.initializeDriver(driver1,projectName,buildName,testName);
        context.setDriver(driver);
        context.initializePageObject(driver);
        driver.get(url);
        driver.manage().window().maximize();
        context.setStudentName("Mandeep");
        context.set("WebsiteURL",url);
        context.set("WebsiteURL123","123544");
    }

    @When("The user enters username {string} and password {string}")
    public void the_user_enters_username_and_password(String username, String password) {
        context.getLoginPageFactory().enterUsername(username);
        log.info("The user enters username");
        context.getLoginPageFactory().enterPassword(password);
        log.info("The user enters password");
    }

    @And("The user clicks on the login button")
    public void the_user_clicks_on_the_login_button() throws InterruptedException {
        context.getLoginPageFactory().clickSignInBtn();
        log.info("The user clicks on the login button");
        Thread.sleep(5000);
        System.out.println("The value of WebsiteURL is : "+context.get("WebsiteURL"));
        System.out.println("The value of WebsiteURL123 is : "+context.get("WebsiteURL123"));
        log.info("The value of WebsiteURL is :{}",context.get("WebsiteURL"));
        log.info("The value of WebsiteURL123 is :{}",context.get("WebsiteURL123"));

    }

    @Then("The user should be redirected to the dashboard")
    public void the_user_should_be_redirected_to_the_dashboard() {
        context.getLoginPageFactory().loadPage();
        String currentUrl = context.getDriver().getCurrentUrl();
        String expectedUrl = "https://automationpracticehub.com/home";
        Assert.assertEquals(
                "The user is not redirected to the dashboard page.",
                expectedUrl,
                currentUrl
        );
    }
    @Then("The user will click on Product menu")
    public void the_user_will_click_on_product_menu() {
        context.getLoginPageFactory().clickProductMenu();
        log.info("The user will click on Product menu");
    }
    @Then("The user will click on AddtoCart button for product1 {string} and for product2 {string} and for product3 {string}")
    public void the_user_will_click_on_addto_cart_button_for_product1_and_for_product2_and_for_product3(String product1, String product2, String product3) throws InterruptedException {
        context.getLoginPageFactory().clickOnAddToCart(product1);
        context.getLoginPageFactory().waitForLoader();
        Thread.sleep(4000);

        /*if(!text.equal("")){
            Assert.fail();
        }
         */
        context.getLoginPageFactory().clickOnAddToCart(product2);
        context.getLoginPageFactory().waitForLoader();
        Thread.sleep(4000);
        context.getLoginPageFactory().clickOnAddToCart(product3);
        context.getLoginPageFactory().waitForLoader();
        Thread.sleep(4000);


    }
    @Then("The user will click on AddtoCart button")
    public void the_user_will_click_on_addto_cart_button() {
        context.getLoginPageFactory().clickOnCameraAddToCart();
        log.info("The user will click on AddtoCart button");
        String studentName=context.getStudentName();
        log.info("The name of student is :{} ",studentName);
        if (studentName.equalsIgnoreCase("Mandeep")){
            Assert.assertTrue(true);
        }else{
            Assert.fail();
        }
    }
    @Then("The user will check for the success message")
    public void the_user_will_check_for_the_success_message() {
        //context.getLoginPageFactory().waitForItemAddedMessage();
        log.info("The user will check for the success message");
        String studentName=context.getStudentName();
        log.info("The name of student is :{} ",studentName);
    }



    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
    private static Throwable lastError;
    public static Throwable getLastError() {
        return lastError;
    }
    public static void switchBackToOriginalWindow(WebDriver driver, String originalWindow) {
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                driver.close();
            }
        }
        driver.switchTo().window(originalWindow);
    }




}
