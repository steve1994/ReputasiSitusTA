package data_structure.feature;

/**
 * Created by steve on 27/03/2016.
 */
public class Spesific_Feature {
    private int tokenCountURL;
    private float averageTokenLengthURL;
    private double[] SLDRatio;
    private int[] inboundLink;
    private float lookupTime;

    public int getTokenCountURL() {
        return tokenCountURL;
    }

    public void setTokenCountURL(int tokenCountURL) {
        this.tokenCountURL = tokenCountURL;
    }

    public float getLookupTime() {
        return lookupTime;
    }

    public void setLookupTime(float lookupTime) {
        this.lookupTime = lookupTime;
    }

    public int[] getInboundLink() {
        return inboundLink;
    }

    public void setInboundLink(int[] inboundLink) {
        this.inboundLink = inboundLink;
    }

    public double[] getSLDRatio() {
        return SLDRatio;
    }

    public void setSLDRatio(double[] SLDRatio) {
        this.SLDRatio = SLDRatio;
    }

    public float getAverageTokenLengthURL() {
        return averageTokenLengthURL;
    }

    public void setAverageTokenLengthURL(float averageTokenLengthURL) {
        this.averageTokenLengthURL = averageTokenLengthURL;
    }

    public Spesific_Feature() {
        tokenCountURL = 0;
        averageTokenLengthURL = 0;
        SLDRatio = new double[3];           // malware, spamming, phishing
        inboundLink = new int[3];           // Google, Yahoo, Bing
        lookupTime = 0;
    }
}
