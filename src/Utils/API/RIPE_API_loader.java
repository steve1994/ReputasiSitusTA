package Utils.API;

import Utils.Converter;
import org.apache.commons.validator.routines.InetAddressValidator;
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
 * Created by steve on 17/03/2016.
 */
public class RIPE_API_Loader {

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

    private static List<String> ripePrefixResultArgument(String jsonString) {
        List<String> ASPrefixesList = new ArrayList<String>();
        try {
            JSONObject mainResponse = new JSONObject(jsonString);
            if (!mainResponse.isNull("data")) {
                JSONObject ASNDataArg = mainResponse.getJSONObject("data");
                if (!ASNDataArg.isNull("prefixes")) {
                    JSONArray listPrefixesArg = ASNDataArg.getJSONArray("prefixes");
                    int size = listPrefixesArg.length();
                    if (listPrefixesArg.length() > 50) {
                        size = 50;
                    }
                    for (int i=0;i<size;i++) {
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

    private static List<String> ripeNameserversResultArguments(String jsonString) {
        List<String> nameServerList = new ArrayList<String>();
        try {
            JSONObject mainResponse = new JSONObject(jsonString);
            if (!mainResponse.isNull("data")) {
                JSONObject NSDataArg = mainResponse.getJSONObject("data");
                if (!NSDataArg.isNull("authoritative_nameservers")) {
                    JSONArray listNS = NSDataArg.getJSONArray("authoritative_nameservers");
                    for (int i=0;i<listNS.length();i++) {
                        nameServerList.add(listNS.getString(i));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return nameServerList;
    }

    public static Boolean checkDomainNameResolved(String host) {
        String urlRequest = "https://stat.ripe.net/data/dns-chain/data.json?resource=" + host;
        String jsonString = getRawJsonResponse(urlRequest);

        Boolean isDomainNSExist = false;
        try {
            JSONObject mainResponse = new JSONObject(jsonString);
            if (!mainResponse.isNull("data")) {
                isDomainNSExist = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isDomainNSExist;
    }

    private static List<String> ripeNameServersPrefixArgument(String jsonString) {
        List<String> listResolvedNameServers = new ArrayList<String>();
        try {
            JSONObject mainResponse = new JSONObject(jsonString);
            if (!mainResponse.isNull("data")) {
                JSONObject NSDataArg = mainResponse.getJSONObject("data");
                if (!NSDataArg.isNull("delegations")) {
                    JSONArray arrayOfDelegation = NSDataArg.getJSONArray("delegations");
                    for (int i=0;i<arrayOfDelegation.length();i++) {
                        JSONArray Delegation = arrayOfDelegation.getJSONArray(i);
                        for (int j=0;j<Delegation.length();j++) {
                            JSONObject keyValuePair = Delegation.getJSONObject(j);
                            String key = keyValuePair.getString("key");
                            String value = keyValuePair.getString("value");
                            if (key.equals("nserver")) {
                                listResolvedNameServers.add(value);
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listResolvedNameServers;
    }

    /**
     * Load IP Prefixes Found from AS Number
     * @param ASNumber
     * @return
     */
    public static List<String> loadASNFromRIPEAPI(int ASNumber) {
        String urlRequestPrefixes = "https://stat.ripe.net/data/as-routing-consistency/data.json?resource=AS" + ASNumber;
        String rawResponseJson = getRawJsonResponse(urlRequestPrefixes);
        return ripePrefixResultArgument(rawResponseJson);
    }

    /**
     * Return list of (authoritative) name servers from a domain / host
     * @param host
     * @return
     */
    public static List<String> loadNameServersFromHost(String host) {
        String urlRequest = "https://stat.ripe.net/data/dns-chain/data.json?resource=" + host;
        String rawResponseJson = getRawJsonResponse(urlRequest);
        return ripeNameserversResultArguments(rawResponseJson);
    }

    /**
     * Return list of name servers from IP Prefix
     * @param IPPrefix
     * @return
     */
    public static List<String> loadNameServersFromIPPrefix(String IPPrefix) {
        String urlRequest = "https://stat.ripe.net/data/reverse-dns/data.json?resource=" + IPPrefix;
        String rawResponseJson = getRawJsonResponse(urlRequest);
        return ripeNameServersPrefixArgument(rawResponseJson);
    }

    public static void main(String[] args) {
        // List BGP Prefixes from AS number
       /* List<String> prefixesList = RIPE_API_Loader.loadASNFromRIPEAPI(Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress("nordiccountry.cz")));
        for (String prefix : prefixesList) {
            System.out.println(prefix);
        }*/
        // List Name servers from Domain / Host
//        List<String> nameServers = RIPE_API_Loader.loadNameServersFromHost("fsdfsdfs.fsdfsdfdsfsdfsd");
//        for (String ns : nameServers) {
//            System.out.println(ns);
//        }

        long begin = System.currentTimeMillis();
        System.out.println(RIPE_API_Loader.loadASNFromRIPEAPI(26415));
        long end = System.currentTimeMillis();

        System.out.println("Waktu eksekusi : " + (end-begin));

        // List Name Servers from Reverse IP Prefix
//        List<String> nsList = RIPE_API_Loader.loadNameServersFromIPPrefix("193.0.0.0/21");
//        for (String ns : nsList) {
//            System.out.println(ns);
//        }
    }
}
