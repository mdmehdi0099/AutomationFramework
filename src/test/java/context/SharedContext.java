package context;


import lombok.*;
import org.openqa.selenium.WebDriver;
import pageFactory.CartPageFactory;
import pageFactory.loginPageFactory;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SharedContext {
    private WebDriver driver;
    private String browser;
    private String buildName;
    private String TestCaseId;
    private String TestName;
    private Throwable scenarioError;
    private String projectName;
    private boolean shouldSkip=true;
    private boolean isTestrail;
    private String accessToken;
    private String studentName;
    private boolean UpdateLambda;
    private boolean UpdateTestrail;
    private final ScenarioContext scenarioContext=new ScenarioContext();
    public ScenarioContext getScenarioContext(){
        return scenarioContext;
    }

    public <T> String get(String key){
        return scenarioContext.get(key);
    }
    public void set(String key,Object value){
        scenarioContext.set(key,value);
    }

    //Page Objects
    private loginPageFactory loginPageFactory;
    private CartPageFactory cartPageFactory;

    public CartPageFactory getCartPageFactory() {
        return cartPageFactory;
    }

    public void setCartPageFactory(CartPageFactory cartPageFactory) {
        this.cartPageFactory = cartPageFactory;
    }

    public loginPageFactory getLoginPageFactory() {
        return loginPageFactory;
    }

    public void setLoginPageFactory(loginPageFactory loginPageFactory) {
        this.loginPageFactory = loginPageFactory;
    }

    public void initializePageObject(WebDriver driver){
        this.loginPageFactory=new loginPageFactory(driver);
        this.cartPageFactory=new CartPageFactory(driver);
    }

}
