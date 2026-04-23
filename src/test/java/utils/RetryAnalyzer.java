package utils;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
        private int count = 0;
        private static int maxCount = 5;

        @Override
        public boolean retry (ITestResult iTestResult){
            if (count < maxCount) {
                count++;
                return true;//re-run the test
            }
            return false;//do not run the test
        }
    }
