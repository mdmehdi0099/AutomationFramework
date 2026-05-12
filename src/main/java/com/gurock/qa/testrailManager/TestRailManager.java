package com.gurock.qa.testrailManager;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TestRailManager {
    public static int TEST_CASE_PASS_STATUS = 1;
    public static int TEST_CASE_FAIL_STATUS = 5;
    public static String TEST_RAIL_USERNAME;
    public static String TEST_RAIL_PASSWORD;
    public static String TEST_RAIL_ENGINE_URL = "https://abc.testrail.io/";
    private static APIClient apiClient;

    //structure
    private static Map<String, Map<String, String>> testCaseMap = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(TestRailManager.class);

    private static void loadTestRailCredentials() throws IOException {
        if (apiClient != null) return;
        TEST_RAIL_USERNAME = getGlobalvalue("TestRailUsername");
        TEST_RAIL_PASSWORD = getGlobalvalue("TestRailPassword");
        disableSSlVerification();
        apiClient = new APIClient(TEST_RAIL_ENGINE_URL);
        apiClient.setUser(TEST_RAIL_USERNAME);
        apiClient.setPassword(TEST_RAIL_PASSWORD);
    }

    public static String getGlobalvalue(String key) throws IOException {
        Properties prop = new Properties();
        FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "/src/test/resources/config/global.properties");
        prop.load(fis);
        return prop.getProperty(key);
    }
    public static void initializeTestCasesFromPlan(){
        try{
            loadTestRailCredentials();
            String testPlanId=getGlobalvalue("TESTPLANID");
            JSONObject plan=(JSONObject)apiClient.sendGet("api/v2/get_plan/"+testPlanId);
            if (plan==null){
                throw new RuntimeException("TestRail plan response is null for plan ID: "+testPlanId);
            }
            String build=plan.get("name")!=null?plan.get("name").toString():"UNKNOWN_BUILD";
            Object projectIdObj=plan.get("project_id");
            if (projectIdObj==null){
                throw new RuntimeException("project_id is missing in Testrail plan: "+testPlanId);
            }

//            JSONArray entries=(JSONArray) plan.get("entries");
//            String build=plan.get("name").toString();
            String projectId=plan.get("project_id").toString();
            JSONObject project=(JSONObject) apiClient.sendGet("api/v2/get_project/"+projectId);
            String projectName=project!=null&&project.get("name")!=null?
                    project.get("name").toString():"UNKNOWN_PROJECT";
            JSONArray entries=(JSONArray) plan.get("entries");
            if (entries==null){
                log.info("No entries found in TestRail plan {}",testPlanId);
                return;
            }
            for(Object entryObj:entries){
                JSONObject entry=(JSONObject) entryObj;
                JSONArray runs=(JSONArray) entry.get("runs");
                if (runs==null){
                    log.info("No runs found in entry");
                    continue;
                }
                for(Object runObj:runs){
                    JSONObject run=(JSONObject) runObj;
                    if (run.get("id")==null){
                        log.info("RUN ID missing, skipping run");
                        continue;
                    }
                    String runId=run.get("id").toString();
                    String browser=run.get("config")!=null?run.get("config").toString():"chrome";

                    Object testResponse=apiClient.sendGet("api/v2/get_tests/"+runId);

                    if(testResponse instanceof JSONObject){
                        JSONObject responseObj=(JSONObject) testResponse;
                        JSONArray tests=(JSONArray)responseObj.get("tests");
                        if (tests!=null){
                            for(Object testObject:tests){
                                JSONObject test=(JSONObject) testObject;
                                String caseId=test.get("case_id").toString();
                                String title=test.get("title").toString();
                                Map<String,String> data=new HashMap<>();
                                data.put("runId",runId);
                                data.put("title",title);
                                data.put("browser",browser);
                                data.put("planId",testPlanId);
                                data.put("projectName",projectName);
                                data.put("build",build);
                                testCaseMap.put(caseId,data);
                            }
                        }else{
                            log.info("No tests array found in the respons for run ID:"+runId);
                        }
                    }else{
                        log.info("Unexpected response type for run ID:"+runId);
                    }
                }
            }
            log.info("Test case map loaded with "+testCaseMap.size()+" cases.");
        } catch (IOException | APIException e) {
            log.info("Error initializing from test Plan: "+e.getMessage());
        }
    }

    public static Map<String,Map<String,String>> getTestCasesFromPlan(){
        return testCaseMap;
    }
    public static Map<String,String> getProjectAndBuildForCase(String caseId){
        if (testCaseMap.containsKey(caseId)){
            Map<String,String> data=testCaseMap.get(caseId);
            Map<String,String> result=new HashMap<>();
            result.put("projectName",data.getOrDefault("projectName","Defaultproject"));
            result.put("buildName",data.getOrDefault("build","DafaultBuild"));
            result.put("browser",data.getOrDefault("browser","DefaultBrowser"));
            log.info("The value of the data is :"+data);
            return result;
        }else{
            log.info("No Mapping found for Case ID:"+caseId);
            return null;
        }
    }



    public static void postResultToTestRail(String caseId,int status,String message) throws IOException{
        try{
            loadTestRailCredentials();
            if(!testCaseMap.containsKey(caseId)){
                log.info("Cannot post result . unknown Case ID: "+caseId);
                return;
            }
            String runId=testCaseMap.get(caseId).get("runId");
            Map<String,Object> data=new HashMap<>();
            data.put("status_id",status);
            data.put("comment",message);
            apiClient.sendPost("api/v2/add_result_for_case/"+runId+"/"+caseId,data);
            log.info("Posted result for case %s in run %s%n",caseId,runId);
        }catch (APIException e){
            log.info("Error posting result: "+e.getMessage());
        }
    }
    public static void disableSSlVerification(){
        try{
            TrustManager[] trustAllCerts=new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };
            SSLContext sc=SSLContext.getInstance("TLS");
            sc.init(null,trustAllCerts,new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        }catch (Throwable e){
            throw new RuntimeException("Failed to disable SSL verification",e);
        }
    }

}
