package loader;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by steve on 27/01/2016.
 */
public class IPDatabaseLoader {
    private static final String pathIPDatabase = "D:\\Informatika\\Materi Semester 8\\IF4092 - Tugas Akhir II dan Seminar" +
            "\\Source Code Tugas Akhir\\TA_Project\\database\\geoip_database\\dbip-country.csv";
    private HashSet<String> listIPAddressStart;
    private HashSet<String> listIPAddressEnd;

    public static byte[] convertIP4AddressToBytesArray(String ip4Address) {
        byte[] ipAddressByte = new byte[4];
        // Split IP Address into four segment
        String[] tokenIPAddress = ip4Address.split("\\.");
        int i = 0;
        for (String token : tokenIPAddress) {
            int ipAddressTokenInt = (byte) Integer.parseInt(token) & 0xFF;
            ipAddressByte[i] = (byte) ipAddressTokenInt;
            i++;
        }
        return ipAddressByte;
    }

    public HashSet<String> getListIPAddressEnd() {
        return listIPAddressEnd;
    }

    public HashSet<String> getListIPAddressStart() {
        return listIPAddressStart;
    }

    public IPDatabaseLoader() {
        listIPAddressEnd = new HashSet<String>();
        listIPAddressStart = new HashSet<String>();
    }

    public void loadIPEndpoint() {
        // Get raw file content
        StringBuffer rawFileContent = new StringBuffer();
        String  thisLine;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(pathIPDatabase));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            while ((thisLine = br.readLine()) != null) {
                rawFileContent.append(thisLine + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringTokenizer token = new StringTokenizer(rawFileContent.toString(),"\n");
        while (token.hasMoreTokens()) {
            String oneRow = (String) token.nextToken();
            String[] oneRowPart = oneRow.split(",\"");
            listIPAddressStart.add(oneRowPart[0].replace("\"",""));
            listIPAddressEnd.add(oneRowPart[1].replace("\"",""));
        }
    }

    public static void main(String[] args) {
        IPDatabaseLoader ip = new IPDatabaseLoader();
        ip.loadIPEndpoint();
       /* String IPAddress = "217.118.112.16";
        byte[] d = IPDatabaseLoader.convertIP4AddressToBytesArray(IPAddress);
        for (byte e : d) {
            System.out.println(e);
        }*/

        Iterator ipAddressStart = ip.getListIPAddressStart().iterator();
        while (ipAddressStart.hasNext()) {
            //String ipAddress = (String) ipAddressStart.next();
            String ipAddress = "66.220.158.68";
            try {
              //  InetAddress addr = InetAddress.getByAddress(IPDatabaseLoader.convertIP4AddressToBytesArray(ipAddress));
                InetAddress addr = InetAddress.getByName(ipAddress);
                System.out.println("Hostname based on IP Start : " + addr.getCanonicalHostName());
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            }
        }

    }
}
