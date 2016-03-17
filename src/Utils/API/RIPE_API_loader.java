package Utils.API;

import Utils.Converter;
import data_structure.ASNIPAddressList;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 17/03/2016.
 */
public class RIPE_API_loader {

    private static List<String> ripePrefixResultArgument(String jsonString) {
        // Struktur data penampung daftar IP Prefix
        List<String> ASPrefixesList = new ArrayList<String>();
        // Parsing parameter pada json response
        try {
            JSONObject mainResponse = new JSONObject(jsonString);
            if (!mainResponse.isNull("data")) {
                JSONObject ASNDataArg = mainResponse.getJSONObject("data");
                if (!ASNDataArg.isNull("prefixes")) {
                    JSONArray listPrefixesArg = ASNDataArg.getJSONArray("prefixes");
                    for (int i=0;i<listPrefixesArg.length();i++) {
                        JSONObject element = listPrefixesArg.getJSONObject(i);
                        String prefix = element.getString("prefix");
                        String IPAddressFromPrefix = prefix.substring(0,prefix.length()-3);
                        if (InetAddressValidator.getInstance().isValidInet4Address(IPAddressFromPrefix)) {
                            ASPrefixesList.add(prefix);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ASPrefixesList;
    }

    public static List<String> loadASNFromRIPEAPI(int ASNumber) {
        // URL Ripe API for Load IP Prefixes from AS number
        String urlRequestPrefixes = "https://stat.ripe.net/data/as-routing-consistency/data.json?resource=AS" + ASNumber;
        // Load Json Response from URL API Above
        StringBuffer response = new StringBuffer();
        try {
            URL url = new URL(urlRequestPrefixes);
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
        return ripePrefixResultArgument(response.toString());
    }

    public static void main(String[] args) {
        List<String> prefixesList = RIPE_API_loader.loadASNFromRIPEAPI(Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress("nordiccountry.cz")));
        for (String prefix : prefixesList) {
            System.out.println(prefix);
        }
    }
}
