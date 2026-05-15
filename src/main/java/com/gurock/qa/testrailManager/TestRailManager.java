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
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
public class TestRailManager {
    public static int TEST_CASE_PASS_STATUS = 1;
    public static int TEST_CASE_FAIL_STATUS = 5;
    public static String TEST_RAIL_USERNAME;
    public static String TEST_RAIL_PASSWORD;
    public static String TEST_RAIL_ENGINE_URL;
    private static APIClient apiClient;
    // Structure: caseId -> {runId, title, browser, planId, projectName, build}
    private static Map<String, Map<String, String>> testCaseMap = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(TestRailManager.class);
    private static void loadTestRailCredentials() throws IOException {
        if (apiClient != null) return;
        log.info("loadTestRailCredentials is called");
        System.out.println("loadTestRailCredentials is called");
        TEST_RAIL_ENGINE_URL = getGlobalValue("TestRailEngineURL");
        TEST_RAIL_USERNAME = getGlobalValue("TESTRAILUSERNAME");
        TEST_RAIL_PASSWORD = getGlobalValue("TESTRAILPASSWORD");
        disableSslVerification();
        apiClient = new APIClient(TEST_RAIL_ENGINE_URL);
        apiClient.setUser(TEST_RAIL_USERNAME);
        apiClient.setPassword(TEST_RAIL_PASSWORD);
        log.info("The value of apiclient is : "+apiClient);
    }
    public static String getGlobalValue(String key) throws IOException {
        Properties prop = new Properties();
        FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "/src/test/resources/config/global.properties");
        prop.load(fis);
        return prop.getProperty(key);
    }
    public static void initializeTestCasesFromPlan1() {
        try {
            loadTestRailCredentials();
            System.out.println("initializeTestCasesFromPlan is called");
            String testPlanId = getGlobalValue("TESTPLANID");
            JSONObject plan = (JSONObject) apiClient.sendGet("api/v2/get_plan/"+testPlanId);
            log.info("the value of plan is :"+plan.toString());
            JSONArray entries = (JSONArray) plan.get("entries");
            // Extract build name from plan's name field
            String build = plan.get("name").toString();
            log.info("The value of build name is : "+build);
            // Extract project name
            String projectId = plan.get("project_id").toString();
            JSONObject project = (JSONObject) apiClient.sendGet("api/v2/get_project/" + projectId);
            String projectName = project.get("name").toString();
            log.info("The value of projectName is : "+projectName);
            for (Object entryObj : entries) {
                JSONObject entry = (JSONObject) entryObj;
                JSONArray runs = (JSONArray) entry.get("runs");
                for (Object runObj : runs) {
                    JSONObject run = (JSONObject) runObj;
                    String runId = run.get("id").toString();
                    // Extract browser info from "config" field, default to "chrome"
                    String browser = run.containsKey("config") ? run.get("config").toString() : "chrome";
                    // Get the list of test cases in the run
                    Object testResponse = apiClient.sendGet("get_tests/" + runId);
                    if (testResponse instanceof JSONObject) {
                        JSONObject responseObj = (JSONObject) testResponse;
                        JSONArray tests = (JSONArray) responseObj.get("tests");
                        if (tests != null) {
                            for (Object testObj : tests) {
                                JSONObject test = (JSONObject) testObj;
                                String caseId = test.get("case_id").toString();
                                String title = test.get("title").toString();
                                Map<String, String> data = new HashMap<>();
                                data.put("runId", runId);
                                data.put("title", title);
                                data.put("browser", browser);
                                data.put("planId", testPlanId);
                                data.put("projectName", projectName);
                                data.put("build", build);
                                testCaseMap.put(caseId, data);
                            }
                        } else {
                            log.info("No 'tests' array found in response for run ID: " + runId);
                        }
                    } else {
                        log.info("Unexpected response type for run ID: " + runId);
                    }
                }

            }
            log.info("Test case map loaded with " + testCaseMap.size() + " cases.");
        } catch (IOException | APIException e) {
            log.info("Error initializing from Test Plan: " + e.getMessage());
        }
    }
    public static void initializeTestCasesFromPlan() {
        try {
            loadTestRailCredentials();
            System.out.println("initializeTestCasesFromPlan is called");
            String testPlanId = getGlobalValue("TestPlanID");
            JSONObject plan = (JSONObject) apiClient.sendGet("api/v2/get_plan/" + testPlanId);
            System.out.println("Plan JSON: {}"+plan.toJSONString());
            System.out.println("Plan JSON: {}"+plan.toString());
            if (plan == null) {
                throw new RuntimeException("TestRail plan response is null for plan ID: " + testPlanId); }
            // ✅ Safe read: plan name (build)
            String build = plan.get("name") != null ? plan.get("name").toString() : "UNKNOWN_BUILD";
            System.out.println("Build name: {}"+build);
            // ✅ Safe read: project ID
            Object projectIdObj = plan.get("project_id");
            if (projectIdObj == null) {
                throw new RuntimeException("project_id is missing in TestRail plan: " + testPlanId);
            }
            String projectId = projectIdObj.toString();
            JSONObject project = (JSONObject) apiClient.sendGet("api/v2/get_project/" + projectId);
            String projectName = project != null && project.get("name") != null ? project.get("name").toString() : "UNKNOWN_PROJECT";
            System.out.println("Project name: {}"+projectName);
            JSONArray entries = (JSONArray) plan.get("entries");
            if (entries == null) {
                System.out.println("No entries found in TestRail plan {}"+testPlanId);
                return;
            }
            for (Object entryObj : entries) {
                JSONObject entry = (JSONObject) entryObj;
                JSONArray runs = (JSONArray) entry.get("runs");
                if (runs == null) {
                    System.out.println("No runs found in entry");
                    continue;
                }
                for (Object runObj : runs) {
                    JSONObject run = (JSONObject) runObj;
                    // ✅ Safe run ID
                    if (run.get("id") == null) {
                        log.info("Run ID missing, skipping run");
                        continue;
                    }
                    String runId = run.get("id").toString();
                    // ✅ Safe browser config
                    String browser = run.get("config") != null ? run.get("config").toString() : "chrome";
                    Object testResponse = apiClient.sendGet("api/v2/get_tests/" + runId);
                    if (!(testResponse instanceof JSONObject)) {
                        log.info("Unexpected test response for run ID: {}", runId);
                        continue;
                    }
                    JSONArray tests = (JSONArray) ((JSONObject) testResponse).get("tests");
                    if (tests == null) {
                        log.info("No tests found for run ID: {}", runId);
                        continue;
                    }
                    for (Object testObj : tests) {
                        JSONObject test = (JSONObject) testObj;
                        if (test.get("case_id") == null) {
                            System.out.println("Test without case_id found, skipping");
                            continue;
                        }
                        String caseId = test.get("case_id").toString();
                        String title = test.get("title") != null ? test.get("title").toString() : "NO_TITLE";
                        Map<String, String> data = new HashMap<>();
                        data.put("runId", runId);
                        data.put("title", title);
                        data.put("browser", browser);
                        data.put("planId", testPlanId);
                        data.put("projectName", projectName);
                        data.put("build", build); testCaseMap.put(caseId, data);
                    }
                }
            }
            log.info("Test case map loaded with {} cases.", testCaseMap.size());
        } catch (IOException | APIException e) {
            log.error("Error initializing from Test Plan", e);
        }
    }
    public static Map<String, Map<String, String>> getTestCasesFromPlan() {
        return testCaseMap;
    }
    public static Map<String, String> getProjectAndBuildForCase(String caseId) {
        if (testCaseMap.containsKey(caseId)) {
            Map<String, String> data = testCaseMap.get(caseId);
            Map<String, String> result = new HashMap<>();
            result.put("projectName", data.getOrDefault("projectName", "DefaultProject"));
            result.put("buildName", data.getOrDefault("build", "DefaultBuild"));
            result.put("browser", data.getOrDefault("browser", "DefaultBrowser"));
            log.info("The value of the data is : "+data);
            return result;
        } else {
            log.info("⚠️ No mapping found for Case ID: " + caseId); return null;
        }
    }
    public static void postResultToTestRail(String caseId, int status, String message) throws IOException {
        try {
            System.out.println("postResultToTestRail---------------------");
            loadTestRailCredentials();
            if (!testCaseMap.containsKey(caseId)) {
                log.info("⚠️ Cannot post result. Unknown Case ID: " + caseId);
                return;
            }
            String runId = testCaseMap.get(caseId).get("runId");
            Map<String, Object> data = new HashMap<>();
            data.put("status_id", status);
            data.put("comment", message);
            apiClient.sendPost("api/v2/add_result_for_case/" + runId + "/" + caseId, data);
            log.info("Posted result for Test case with ID as {} in run {}", caseId, runId);
        } catch (APIException e) {
            log.info("Error posting result: " + e.getMessage());
        }
    }
    public static void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to disable SSL verification", e);
        }
    }
}