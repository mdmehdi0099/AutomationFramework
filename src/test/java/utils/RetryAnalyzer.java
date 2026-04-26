package utils;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int count = 0;
    private static final int MAX_RETRY = 2;

    @Override
    public boolean retry(ITestResult result) {
        if (count < MAX_RETRY) {
            System.out.println("The value of retry is : +count");
            count++;
            result.setAttribute("retry", true);

            return true;
        }
        return false;
    }

    public boolean isFinalAttempt() {
        return count >= MAX_RETRY;
    }
}
