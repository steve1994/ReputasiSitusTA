package Utils.API;

import data_structure.feature.Trust_Feature;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by steve on 28/01/2016.
 */
public class WOT_API_Loader {
    private static final String wotAPIKey = "ec2210fe8584a03dc89a9dd05e3c7c1754fa3510";

    private static Integer[] valueOfCategoryEstimateWOT(HashMap<String,Integer> mappingKeyValueCategories) {
        Integer[] categoryValueEstimate = new Integer[4]; // negative,questionable,neutral.positive;
        for (int i=0;i<4;i++) {
            categoryValueEstimate[i] = 0;
        }
        for (Map.Entry m : mappingKeyValueCategories.entrySet()) {
            int keyNumber = Integer.parseInt((String) m.getKey());
            if (keyNumber / 100 == 1) {
                categoryValueEstimate[0] = (Integer) m.getValue();
            } else if (keyNumber / 100 == 2) {
                categoryValueEstimate[1] = (Integer) m.getValue();
            } else if (keyNumber / 100 == 3) {
                categoryValueEstimate[2] = (Integer) m.getValue();
            } else if (keyNumber / 100 == 4) {
                switch (keyNumber) {
                    case 401 :
                        categoryValueEstimate[0] = (Integer) m.getValue(); break;
                    case 402 :
                    case 403 :
                        categoryValueEstimate[1] = (Integer) m.getValue(); break;
                    case 404 :
                        categoryValueEstimate[3] = (Integer) m.getValue(); break;
                }
            } else if (keyNumber / 100 == 5) {
                categoryValueEstimate[3] = (Integer) m.getValue();
            }
        }
        return categoryValueEstimate;
    }

    private static Integer[] valueOfBlacklistBooleanWOT(HashSet<String> listTypes) {
        Integer[] listTypeBlacklists = new Integer[4];
        for (int i=0;i<4;i++) {
            listTypeBlacklists[i] = 0;
        }
        for (String type : listTypes) {
            if (type.equals("malware")) {
                listTypeBlacklists[0] = 1;
            } else if (type.equals("phishing")) {
                listTypeBlacklists[1] = 1;
            } else if (type.equals("scam")) {
                listTypeBlacklists[2] = 1;
            } else if (type.equals("spam")) {
                listTypeBlacklists[3] = 1;
            }
        }
        return listTypeBlacklists;
    }

    private static Trust_Feature wotResultArgument(String jsonResponse, String hostName) {
        // Model to store WOT data
        Trust_Feature trustFeature = new Trust_Feature();
        // Filter and Parse json response to get required data
        String[] tokenResponse = jsonResponse.replace("process","").split("[()]");
        String cleanResponseJson = tokenResponse[1];
        try {
            JSONObject obj = new JSONObject(cleanResponseJson);
            JSONObject detail = obj.getJSONObject(hostName);
            // Get trustworthy value
            if (!detail.isNull("0")) {
                JSONArray pairTrustworthy = detail.getJSONArray("0");
                String[] tokenPairTrustworthy = pairTrustworthy.toString().split("[\\[\\],]");
                Integer estimateValuesTrust = Integer.parseInt(tokenPairTrustworthy[1]);
                Integer confidenceValuesTrust = Integer.parseInt(tokenPairTrustworthy[2]);
                Pair<Integer,Integer> estimateConfidencePair = new Pair<Integer, Integer>(estimateValuesTrust,confidenceValuesTrust);
                trustFeature.setTrustWorthinessPairValues(estimateConfidencePair);
            }
            // Get safetychildren value
            if (!detail.isNull("4")) {
                JSONArray pairSafetyChildren = detail.getJSONArray("4");
                String[] tokenPairSafetyChildren = pairSafetyChildren.toString().split("[\\[\\],]");
                Integer estimateValuesSafety = Integer.parseInt(tokenPairSafetyChildren[1]);
                Integer confidenceValuesSafety = Integer.parseInt(tokenPairSafetyChildren[2]);
                Pair<Integer,Integer> estimateConfidencePair = new Pair<Integer, Integer>(estimateValuesSafety,confidenceValuesSafety);
                trustFeature.setChildSafetyPairValues(estimateConfidencePair);
            }
            // Get categories detail
            if (!detail.isNull("categories")) {
                JSONObject pairCategories = detail.getJSONObject("categories");
                HashMap<String,Integer> keyEstimatePair = new HashMap<String, Integer>();
                Iterator keyCategoriesList = pairCategories.keys();
                while (keyCategoriesList.hasNext()) {
                    String key = (String) keyCategoriesList.next();
                    int estimateValue = pairCategories.getInt(key);
                    keyEstimatePair.put(key,estimateValue);
                }
                trustFeature.setCategoryEstimateValues(valueOfCategoryEstimateWOT(keyEstimatePair));
            }
            // Get blacklist
            if (!detail.isNull("blacklists")) {
                JSONObject blacklists = detail.getJSONObject("blacklists");
                HashSet<String> listTypes = new HashSet<String>();
                Iterator typeBlacklists = blacklists.keys();
                while (typeBlacklists.hasNext())
                {
                    String key = (String) typeBlacklists.next();
                    listTypes.add(key);
                }
                trustFeature.setBlacklistIncluded(valueOfBlacklistBooleanWOT(listTypes));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return trustFeature;
    }

    public static Trust_Feature loadAPIWOTForSite(String hostName) {
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
        List<Object> trustFeature = new ArrayList<Object>();

        long before = System.currentTimeMillis();
        Trust_Feature model = WOT_API_Loader.loadAPIWOTForSite("0000love.net");
        System.out.println("CATEGORIES");
        for (int i=0;i<4;i++) {
            System.out.println(model.getCategoryEstimateValues()[i]);
            trustFeature.add(model.getCategoryEstimateValues()[i]);
        }
        System.out.println("BLACKLISTS");
        for (int i=0;i<4;i++) {
            System.out.println(model.getBlacklistIncluded()[i]);
            trustFeature.add(model.getBlacklistIncluded()[i]);
        }
        System.out.println("TRUST");
        System.out.println(model.getTrustWorthinessPairValues().getKey()); trustFeature.add(model.getTrustWorthinessPairValues().getKey());
        System.out.println(model.getTrustWorthinessPairValues().getValue()); trustFeature.add(model.getTrustWorthinessPairValues().getValue());
        System.out.println("SAFETY");
        System.out.println(model.getChildSafetyPairValues().getKey()); trustFeature.add(model.getChildSafetyPairValues().getKey());
        System.out.println(model.getChildSafetyPairValues().getValue()); trustFeature.add(model.getChildSafetyPairValues().getValue());

        System.out.println("============================================================================");
        // TES ISI FITUR KE INSTANCE WEKA
        double[] values = new double[trustFeature.size()];
        for (int i=0;i<trustFeature.size();i++) {
            values[i] = new Double(trustFeature.get(i).toString());
        }
        for (double d : values) {
            System.out.println(d);
        }
        long after = System.currentTimeMillis();

        System.out.println("Waktu eksekusi : " + (after-before));
    }
}
