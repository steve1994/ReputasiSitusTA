package Utils;

import data_structure.WOTModel;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by steve on 28/01/2016.
 */
public class APILoader {
    private static final String wotAPIKey = "ec2210fe8584a03dc89a9dd05e3c7c1754fa3510";


    private static WOTModel wotResultArgument(String jsonResponse, String hostName) {
        // Model to store WOT data
        WOTModel wotModel = new WOTModel();
        // Filter and Parse json response to get required data
        String[] tokenResponse = jsonResponse.replace("process","").split("[()]");
        String cleanResponseJson = tokenResponse[1];
        try {
            JSONObject obj = new JSONObject(cleanResponseJson);
            JSONObject detail = obj.getJSONObject(hostName);
            // Get trustworthy value
            JSONArray pairTrustworthy = detail.getJSONArray("0");
            String[] tokenPairTrustworthy = pairTrustworthy.toString().split("[\\[\\],]");
            wotModel.setTrustWorthinessEstimate(Integer.parseInt(tokenPairTrustworthy[0]));
            wotModel.setTrustWorthinessConfidence(Integer.parseInt(tokenPairTrustworthy[1]));
            // Get safetychildren value
            JSONArray pairSafetyChildren = detail.getJSONArray("4");
            String[] tokenPairSafetyChildren = pairSafetyChildren.toString().split("[\\[\\],]");
            wotModel.setChildSafetyEstimate(Integer.parseInt(tokenPairSafetyChildren[0]));
            wotModel.setChildSafetyConfidence(Integer.parseInt(tokenPairSafetyChildren[1]));
            // Get categories detail
            JSONObject pairCategories = detail.getJSONObject("categories");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return wotModel;
    }

    public static WOTModel loadAPIWOTForSite(String hostName) {
        // URL API with its parameter
        String urlRequest = "http://api.mywot.com/0.4/public_link_json2?hosts=" + hostName + "/&callback=process&key=" + wotAPIKey;
        // Load raw string response from above url api
        StringBuffer response = new StringBuffer();
        try {
            URL url = new URL(urlRequest);
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line + "\n");
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return (wotResultArgument(response.toString(),hostName));
    }

    public static void main(String[] args) {
        APILoader.loadAPIWOTForSite("example.com");
    }
}
