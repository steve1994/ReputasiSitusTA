package Utils.API;

import Utils.Database.EksternalFile;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.SignatureMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by steve on 04/08/2016.
 */
public class BrightCloud_API_Loader {

    private static final String REST_ENDPOINT = "http://thor.brightcloud.com/rest";
    private static final String URI_INFO_PATH = "/uris";

    private static final String CONSUMER_KEY = "dpf43f3p2l4k3l03";
    private static final String CONSUMER_SECRET = "kd94hf93k423kf44";

    private static final String ACCESS_KEY_WEB_SHRINKER = "tA2nl1CRV7aVVvhkaBtE";
    private static final String SECRET_KEY_WEB_SHRINKER = "87bwJIMVIhJL4v2m4voT";

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

    private static List<String> categoriesParserWebShrinker(String jsonString) {
        List<String> categories = new ArrayList<String>();
        try {
            JSONObject mainResponse = new JSONObject(jsonString);
            if (!mainResponse.isNull("data")) {
                JSONArray arrayDataURL = mainResponse.getJSONArray("data");
                JSONObject objectDataURL = arrayDataURL.getJSONObject(0);
                if (!objectDataURL.isNull("categories")) {
                    JSONArray listCategories = objectDataURL.getJSONArray("categories");
                    for (int i=0;i<listCategories.length();i++) {
                        categories.add(((String) listCategories.get(i)).replace("\"",""));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return categories;
    }

    private static String url_request_generator_webshrinker(String url) {
        byte[] urlBytes = url.getBytes(StandardCharsets.UTF_8);
        String request = "categories/v2/" + Base64.getEncoder().encodeToString(urlBytes) + "?key=" + ACCESS_KEY_WEB_SHRINKER;
        String hash = "";
        try {
            MessageDigest messageDigest = MessageDigest .getInstance("MD5");
            messageDigest.update((SECRET_KEY_WEB_SHRINKER + ":" + request).getBytes());
            byte byteData[] = messageDigest.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            hash = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "https://api.webshrinker.com/" + request + "&hash=" + hash;
    }

    public static List<String> getListCategoriesFromWebShrinker(String url) {
        return categoriesParserWebShrinker(getRawJsonResponse(url_request_generator_webshrinker(url)));
    }

    public static void main(String[] args) {
//        List<String> listPopularSites = EksternalFile.loadSitesTrainingList(4).getKey();
//        int belowIndex = 75;
//        int upperIndex = 84;
//        StringBuffer sb = new StringBuffer();
//        for (int i=belowIndex;i<upperIndex;i++) {
//            List<String> listCategoriesThisSite = BrightCloud_API_Loader.getListCategoriesFromWebShrinker(listPopularSites.get(i));
//            for (String category : listCategoriesThisSite) {
//                sb.append(category + " ; ");
//                System.out.println(category);
//            }
//            sb.append("\n");
//        }
//        EksternalFile.saveRawContentToEksternalFile(sb.toString(),"database/top_popular_websites/a.txt");

//        List<String> listPopularSites = EksternalFile.loadSitesTrainingList(4).getKey();
//        List<String> listTopNewsPathAlexa = EksternalFile.loadSitesTrainingList(6).getKey();
//        for (int i=0;i<listPopularSites.size();i++) {
//            for (int j=0;j<listTopNewsPathAlexa.size();j++) {
//                if (listPopularSites.get(i).equals(listTopNewsPathAlexa.get(j))) {
//                    System.out.println(listPopularSites.get(i));
//                }
//            }
//        }

        System.out.print("Enter uri: ");
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        String uri = null;
        try {
            uri = consoleReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String endpoint = REST_ENDPOINT + URI_INFO_PATH + "/" + uri;

        HttpURLConnection request = null;
        BufferedReader rd = null;
        StringBuilder response = null;

        try{
            URL endpointUrl = new URL(endpoint);
            request = (HttpURLConnection)endpointUrl.openConnection();
            request.setRequestMethod("GET");

            try{
                OAuthConsumer consumer = new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
                consumer.setTokenWithSecret("", "");
                consumer.sign(request);
            } catch(OAuthMessageSignerException ex){
                System.out.println("OAuth Signing failed - " + ex.getMessage());
            } catch(OAuthExpectationFailedException ex){
                System.out.println("OAuth failed - " + ex.getMessage());
            }

            request.connect();

            rd  = new BufferedReader(new InputStreamReader(request.getInputStream()));
            response = new StringBuilder();
            String line = null;
            while ((line = rd.readLine()) != null){
                response.append(line + '\n');
            }
        } catch (MalformedURLException e) {
            System.out.println("Exception: " + e.getMessage());
            //e.printStackTrace();
        } catch (ProtocolException e) {
            System.out.println("Exception: " + e.getMessage());
            //e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            //e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            //e.printStackTrace();
        } finally {
            try{
                request.disconnect();
            } catch(Exception e){
            }

            if(rd != null){
                try{
                    rd.close();
                } catch(IOException ex){
                }
                rd = null;
            }
        }

        System.out.println( (response != null) ? response.toString() : "No Response");
    }
}
