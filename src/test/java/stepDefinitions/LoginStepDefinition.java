//package stepDefinitions;
//
//import context.SharedContext;
//import io.cucumber.java.en.And;
//import io.cucumber.java.en.Given;
//import io.cucumber.java.en.Then;
//import io.cucumber.java.en.When;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import utils.BaseClass;
//
//public class LoginStepDefinition {
//    BaseClass baseClass = new BaseClass();
//    private final SharedContext context;
//
//
//    public LoginStepDefinition(SharedContext context, BaseClass baseClass) {
//        this.context = context;
//        this.baseClass = baseClass;
//    }
//    private static final Logger log = LoggerFactory.getLogger(LoginStepDefinition.class);
//
//    @Given("The user navigates to {string}")
//    public void the_user_navigates_to(String string) {
//        log.info("The user navigates to {string}");
//    }
//
//    @When("The user enters username {string} and password {string}")
//    public void the_user_enters_username_and_password(String string, String string2) {
//        log.info("The user enters username {string} and password {string}");
//    }
//
//    @And("The user clicks on the login button")
//    public void the_user_clicks_on_the_login_button() {
//        log.info("The user clicks on the login button");
//    }
//
//    @Then("The user should be redirected to the dashboard")
//    public void the_user_should_be_redirected_to_the_dashboard() {
//        log.info("The user should be redirected to the dashboard");
//    }
//
//
//
//}
