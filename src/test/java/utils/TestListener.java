package utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
public class TestListener implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
        ITestListener.super.onTestStart(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ITestListener.super.onTestSuccess(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Object testClass=result.getInstance();
        try {
            Field driverField=testClass.getClass().getSuperclass().getDeclaredField("driver");
            driverField.setAccessible(true);
            WebDriver driver=(WebDriver) driverField.get(testClass);

            //TakesScreenshot ts=(TakesScreenshot) driver;
            //File src=ts.getScreenshotAs(OutputType.FILE);
            //FileUtils.copyFile(src,new File("screenshots/"+result.getName()+".png"));

            //takeSnapshot

//            //create a folder where screenshot is stored if not found
//            Path folder= Paths.get(System.getProperty("user.dir")+"/src/test/resources/snapshot");
//            Files.createDirectories(folder);
//
//            //TimeStamps for unique file names
//            String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//            String fullFileName=filename+"_"+timeStamp+".png";
//
//            //capture the screenshot
//            File screenshot= ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
//
//            //save to the desired location
//            Path destinationFolder=folder.resolve(fullFileName);
//            Files.copy(screenshot.toPath(),destinationFolder, StandardCopyOption.REPLACE_EXISTING);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        //TakesScreenshot takesScreenshot=driver;
        ITestListener.super.onTestFailure(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ITestListener.super.onTestSkipped(result);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        ITestListener.super.onTestFailedButWithinSuccessPercentage(result);
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        ITestListener.super.onTestFailedWithTimeout(result);
    }

    @Override
    public void onStart(ITestContext context) {
        ITestListener.super.onStart(context);
    }

    @Override
    public void onFinish(ITestContext context) {
        ITestListener.super.onFinish(context);
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
