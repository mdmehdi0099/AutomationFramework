package pageFactory;

import exceptions.element.ElementInteractionException;
import org.openqa.selenium.*;
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
        this.wait=new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @FindBy(xpath = "//input[@id='username']")
    WebElement username;

    public void enterUsername(String userName){
       try {
           wait.until(ExpectedConditions.visibilityOf(username)).sendKeys(userName);
       }catch (TimeoutException | NoSuchElementException e){
           throw new ElementInteractionException("Unable to enter username:", e);
       }
    }

    @FindBy(xpath = "//input[@id='password']")
    WebElement password;

    public void enterPassword(String password1){
        try {
            wait.until(ExpectedConditions.visibilityOf(password)).sendKeys(password1);
        }catch (TimeoutException | NoSuchElementException e){
            throw new ElementInteractionException("Unable to enter password:", e);
        }
    }

    @FindBy(xpath = "//button[contains(text(),'Sign In')]")
    WebElement signIn;

    public void clickSignInBtn(){
        try {
            wait.until(ExpectedConditions.visibilityOf(signIn)).click();
        }catch (TimeoutException | NoSuchElementException e){
            throw new ElementInteractionException("Unable to click signIn button:", e);
        }
    }

    @FindBy(xpath = "//li[contains(text(),'Products')]")
    WebElement product;
    public void clickProductMenu(){
        try {
            wait.until(ExpectedConditions.visibilityOf(product)).click();
        }catch (TimeoutException | NoSuchElementException e){
           throw new ElementInteractionException("Unable to click signIn button:", e);
        }
    }


    @FindBy(xpath = "(//button[contains(text(),'Add to cart')])[2]")
    WebElement cameraAddToCart;
    public void clickOnCameraAddToCart(){
        try {
            wait.until(ExpectedConditions.visibilityOf(cameraAddToCart)).click();
        }catch (TimeoutException | NoSuchElementException e){
            throw new ElementInteractionException("Unable to click cameraAddToCart:", e);
        }
    }

    @FindBy(xpath = "//p[contains(text(),'Item added successfully')]")
    WebElement itemAddedMessage;
    public void waitForItemAddedMessage() {
        wait.until(ExpectedConditions.visibilityOf(itemAddedMessage));
    }

    public void loadPage() {
        loginPageFactory.waitForPageLoad(wait);
    }
    public static void waitForPageLoad(WebDriverWait wait) {
        wait.until(webDriver ->
                ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState")
                        .equals("complete"));
    }






}
