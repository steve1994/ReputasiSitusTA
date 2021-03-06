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
    private static final String mxToolboxAPIKey = "b2ad9c46-f001-4cc5-b685-8a7b33e111e8";

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

    private static List<String> parseIPAddressArgument(String rawJsonResponse) {
        List<String> listIPAddress = new ArrayList<String>();
        try {
            JSONObject mainJSONResponse = new JSONObject(rawJsonResponse);
            if (!mainJSONResponse.isNull("Information")) {
                JSONArray arrayNameServers = mainJSONResponse.getJSONArray("Information");
                for (int i=0;i<arrayNameServers.length();i++) {
                    JSONObject nameServerObject = arrayNameServers.getJSONObject(i);
                    if (!nameServerObject.isNull("IP Address")) {
                        String IPAddress = nameServerObject.getString("IP Address");
                        listIPAddress.add(IPAddress);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listIPAddress;
    }

    /**
     * Return list of time-to-live each name servers owned by domain
     * @param url
     * @return
     */
    public static List<Integer> listNameServerTimeToLive(String url) {
        String urlRequest = "http://api.mxtoolbox.com/api/v1/lookup/dns/" + url + "?authorization=" + mxToolboxAPIKey;
        String jsonResponse = getRawJsonResponse(urlRequest);
        return parseNameServerTTLArgument(jsonResponse);
    }

    /**
     * Return list of time-to-live each DNS A Records from domain
     * @param url
     * @return
     */
    public static List<Integer> listIPAddressTimeToLive(String url) {
        String urlRequest = "http://api.mxtoolbox.com/api/v1/lookup/a/" + url + "?authorization=" + mxToolboxAPIKey;
        String jsonResponse = getRawJsonResponse(urlRequest);
        return parseNameServerTTLArgument(jsonResponse);
    }

    /**
     * Convert host name into its corresponding IP Address
     * @param url
     * @return
     */
    public static String HostNameToIPAddress(String url) {
        String urlRequest = "http://api.mxtoolbox.com/api/v1/lookup/a/" + url + "?authorization=" + mxToolboxAPIKey;
        String jsonResponse = getRawJsonResponse(urlRequest);
        List<String> IPAddresses = parseIPAddressArgument(jsonResponse);
        if (IPAddresses.size() > 0) {
            return parseIPAddressArgument(jsonResponse).get(0);
        } else {
            return "";
        }
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
