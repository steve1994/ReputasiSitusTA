package Utils.API;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by steve on 04/08/2016.
 */
public class BrightCloud_API_Loader {

    public static final String REST_ENDPOINT = "http://thor.brightcloud.com/rest";
    public static final String URI_INFO_PATH = "/uris";

    public static final String CONSUMER_KEY = "dpf43f3p2l4k3l03";
    public static final String CONSUMER_SECRET = "kd94hf93k423kf44";

    public static void main(String[] args) {
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
