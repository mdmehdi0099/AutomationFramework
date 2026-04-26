package utils;

import com.gurock.qa.testrailManager.TestRailManager;
import context.SharedContext;
import exceptions.config.ConfigException;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

public class Hooks {
    private final SharedContext context;

    public Hooks(SharedContext context) {
        this.context = context;
    }
    private static final Logger log=LoggerFactory.getLogger(Hooks.class);

    @BeforeAll
    public static void beforeAll() throws ConfigException {
        ConfigReader.load();
    }
    @Before
    public void beforeScenario(Scenario scenario) throws IOException, ConfigException {
        Collection<String> tags=scenario.getSourceTagNames();
        Boolean isTestrail=Boolean.valueOf(ConfigReader.get("TestrailReadTestcase","false"));
        context.setTestrail(isTestrail);

        String testCaseId=null;
        for(String tag:tags){
            if(tag.startsWith("@C")){
                testCaseId=tag.replace("@C","").trim();
                context.setTestCaseID(testCaseId);
                break;
            }
        }
        context.setTestName(scenario.getName());
        try{
            String projectName="";
            if(context.isTestrail()){
                String projectID=ConfigReader.get("projectID","1");
                projectName=getProjectName(projectID);
                context.setProjectName(projectName);
            }else{
                projectName=ConfigReader.get("projectName","projectName");
                context.setProjectName(projectName);
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        //setBuildName
        try{
            String buildName="";
            if(context.isTestrail()){
                String buildID=ConfigReader.get("TESTPLANID","1");
                buildName=getBuildName(buildID);
                context.setBuildName(buildName);
            }else{
                buildName=ConfigReader.get("BuildName","BuildName");
                context.setBuildName(buildName);
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        if (context.isTestrail()){
            if (testCaseId!=null){
                Map<String,Map<String,String>> testCasesFromPlan= TestRailManager.getTestCasesFromPlan();
                if (testCasesFromPlan.containsKey(testCaseId)){
                    Map<String,String> caseDetails=testCasesFromPlan.get(testCaseId);
                    context.setBrowser(caseDetails.getOrDefault("browser","chrome"));
                }
            }
        }else{
            String browser=ConfigReader.get("browser","browser");
            context.setBrowser(browser);
        }
    }
    @After
    public void afterScenario(Scenario scenario) throws Exception{
        String testCaseId=context.getTestCaseID();
        ByteArrayOutputStream consoleOutput=new ByteArrayOutputStream();
        PrintStream originalOut=System.out;
        System.setOut(new PrintStream(consoleOutput));
        String status="passed";
        String comment="PASSED";
        try{
            if(scenario.isFailed()){
                status="failed";
                comment="FAILED: "+scenario.getStatus().toString()+"\n"+(scenario.getStatus().name().equals("FAILED")?scenario.getName():"");
            }
            System.setOut(originalOut);
            comment+="\nConsole Output:\n"+consoleOutput.toString();
            comment+="\nScenario: "+scenario.getName();
            comment+="\nTags: "+scenario.getSourceTagNames();

            if (context.isTestrail()){
                if (testCaseId!=null && !testCaseId.isEmpty()){
                    if (status.equals("passed")){
                        TestRailPassUpdate(testCaseId,comment);
                    }else{
                        TestRailFailUpdate(testCaseId,comment);
                    }
                }
            }

        }catch (Exception e){
            System.err.println("Error during afterscenario logic : "+e.getMessage());
            e.printStackTrace();
        }finally{
            System.out.println("Driver quit after scenario : "+scenario.getName());
        }
    }

    public void TestRailPassUpdate(String testCaseId,String message){
        try{
            int status=TestRailManager.TEST_CASE_PASS_STATUS;
            TestRailManager.postResultToTestRail(testCaseId,status,message);
        } catch (IOException e) {
            log.info("Error updating TestRail (Pass): "+e.getMessage());
        }
    }
    public void TestRailFailUpdate(String testCaseId,String message){
        try{
            int status=TestRailManager.TEST_CASE_FAIL_STATUS;
            TestRailManager.postResultToTestRail(testCaseId,status,message);
        } catch (IOException e) {
            log.info("Error updating TestRail (Fail): "+e.getMessage());
        }
    }


    public static String getProjectName(String projectID) throws IOException, ConfigException {
        String projectName="";
        String URL=ConfigReader.get("TESTRAILAPIProjectNameURL","testRail")+projectID;
        String username1=ConfigReader.get("TEStrailusername","testRailUserName");
        String password= ConfigReader.get("testrailPassword","TestrailPassword");
        //sending the get request
        Response response= RestAssured.given().auth()
                .preemptive().basic(username1,password)
                .header("Accept","application/json")
                .header("Content-Type","application/json")
                .when()
                .get(URL);
        if (response.getStatusCode()==200){
            projectName=response.jsonPath().getString("name");
        }
        return projectName;
    }

    public static String getBuildName(String buildID) throws IOException, ConfigException {
        String buildName="";
        String URL=ConfigReader.get("TESTRAILAPIProjectNameURL","testrailAPIProjectNameURL")+buildID;
        String username1=ConfigReader.get("TEStrailusername","TestRailUsernmae");
        String password= ConfigReader.get("testrailPassword","TestrailPassword");
        //sending the get request
        Response response= RestAssured.given().auth()
                .preemptive().basic(username1,password)
                .header("Accept","application/json")
                .header("Content-Type","application/json")
                .when()
                .get(URL);
        if (response.getStatusCode()==200){
            buildName=response.jsonPath().getString("name");
        }
        return buildName;
    }





}
