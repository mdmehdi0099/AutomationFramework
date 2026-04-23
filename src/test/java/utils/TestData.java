package utils;
import org.testng.annotations.DataProvider;

import java.io.IOException;
public class TestData {
    public static ReadExcelFile readExcelFile=new ReadExcelFile();

    public static final int loginRow=1;
    public static final int username=0;
    public static final int password=1;

    @DataProvider(name="loginData")
    public static Object[][] fetchExcelData() throws IOException {
        return new Object[][]{
                {
                        readExcelFile.readDataFromExcel(loginRow, username),
                        readExcelFile.readDataFromExcel(loginRow, password)
                }
        };
    }

    @DataProvider(name="loginData2")
    public static Object[][] fetchExcelData2() throws IOException {
        return new Object[][]{
                {
                        readExcelFile.readDataFromExcel(loginRow, username)
                }
        };
    }
}
