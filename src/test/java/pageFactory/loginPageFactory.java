package pageFactory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class loginPageFactory {

    WebDriver driver;
    WebDriverWait wait;
    public loginPageFactory(WebDriver driver){
        this.driver=driver;
        PageFactory.initElements(driver,this);
        this.wait=new WebDriverWait(driver, Duration.ofSeconds(300));
    }

    @FindBy(xpath = "//input[@id='username']")
    WebElement username;

    public void enterUsername(String userName){
        wait.until(ExpectedConditions.visibilityOf(username)).sendKeys(userName);
        //username.sendKeys(userName);
    }


    @FindBy(xpath = "//input[@id='password']")
    WebElement password;

    public void enterPassword(String password1){
        wait.until(ExpectedConditions.visibilityOf(password)).sendKeys(password1);
    }

    @FindBy(xpath = "//button[contains(text(),'Sign In')]")
    WebElement signIn;

    public void clickSignInBtn(){
        wait.until(ExpectedConditions.visibilityOf(signIn)).click();

    }










}
