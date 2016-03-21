package Utils.API;

import Utils.Database.EksternalFile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 21/03/2016.
 */
public class MXToolbox_API_Loader {
    private static String getRawJsonResponse(String urlRequest) {
        StringBuffer response = new StringBuffer();
        try {
            URL url = new URL(urlRequest);
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(url.openStream()));
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
        return response.toString();
    }

    /*private static Integer parseIPTTLArgument(String rawJsonResponse) {
        Integer TTL;
        try {
            JSONObject mainJsonResponse = new JSONObject(rawJsonResponse);
            if (!mainJsonResponse.isNull("Information")) {

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TTL;
    }*/

    private static List<Integer> parseNameServerTTLArgument(String rawJsonResponse) {
         List<Integer> listTTL = new ArrayList<Integer>();
         try {
             JSONObject mainJSONResponse = new JSONObject(rawJsonResponse);
             if (!mainJSONResponse.isNull("Information")) {
                 JSONArray arrayNameServers = mainJSONResponse.getJSONArray("Information");
                 for (int i=0;i<arrayNameServers.length();i++) {
                     JSONObject nameServerObject = arrayNameServers.getJSONObject(i);
                     if (!nameServerObject.isNull("TTL")) {
                         String ttlString = nameServerObject.getString("TTL");
                         int TTL; // in minute
                         if (ttlString.contains("min")) {
                             TTL = Integer.parseInt(ttlString.replace(" min",""));
                         } else if (ttlString.contains("hrs")) {
                             TTL = Integer.parseInt(ttlString.replace(" hrs","")) * 60;
                         } else {
                             TTL = 0;
                         }
                         listTTL.add(TTL);
                     }
                 }
             }
         } catch (JSONException e) {
             e.printStackTrace();
         }

         return listTTL;
    }

    /**
     * Return list of time-to-live each name servers owned by domain
     * @param url
     * @return
     */
    public static List<Integer> listNameServerTimeToLive(String url) {
        String urlRequest = "http://api.mxtoolbox.com/api/v1/lookup/dns/" + url + "?authorization=b2ad9c46-f001-4cc5-b685-8a7b33e111e8";
        String jsonResponse = getRawJsonResponse(urlRequest);
        return parseNameServerTTLArgument(jsonResponse);
    }

    /**
     * Return list of time-to-live each DNS A Records from domain
     * @param url
     * @return
     */
    public static List<Integer> listIPAddressTimeToLive(String url) {
        String urlRequest = "http://api.mxtoolbox.com/api/v1/lookup/a/" + url + "?authorization=b2ad9c46-f001-4cc5-b685-8a7b33e111e8";
        String jsonResponse = getRawJsonResponse(urlRequest);
        return parseNameServerTTLArgument(jsonResponse);
    }

    public static void main(String[] args) {
        List<String> listSites = EksternalFile.loadSitesTrainingList(1).getKey();
        for (String site : listSites) {
            System.out.println(site);
            List<Integer> listTTLs = MXToolbox_API_Loader.listIPAddressTimeToLive(site);
            System.out.println(listTTLs);
        }
    }
}
