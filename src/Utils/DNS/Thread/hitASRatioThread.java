package Utils.DNS.Thread;

/**
 * Created by steve on 08/04/2016.
 */
public class hitASRatioThread extends Thread {
    private int controlURLASN;
    private String siteComparerURL;

    public hitASRatioThread(int controlURLASN, String siteComparerURL) {
        this.controlURLASN = controlURLASN;
        this.siteComparerURL = siteComparerURL;
    }

    @Override
    public void run() {

    }
}
