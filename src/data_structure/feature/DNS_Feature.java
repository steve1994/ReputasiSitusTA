package data_structure.feature;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 27/03/2016.
 */
public class DNS_Feature {
    private Float[] popularTLDRatio;
    private Float[] hitASRatio;
    private float distributionNSAS;
    private int numNameServer;
    private List<Integer> listNSTTL;
    private List<Integer> listDNSRecordTTL;

    public Float[] getPopularTLDRatio() {
        return popularTLDRatio;
    }

    public void setPopularTLDRatio(Float[] popularTLDRatio) {
        this.popularTLDRatio = popularTLDRatio;
    }

    public List<Integer> getListDNSRecordTTL() {
        return listDNSRecordTTL;
    }

    public void setListDNSRecordTTL(List<Integer> listDNSRecordTTL) {
        this.listDNSRecordTTL = listDNSRecordTTL;
    }

    public List<Integer> getListNSTTL() {
        return listNSTTL;
    }

    public void setListNSTTL(List<Integer> listNSTTL) {
        this.listNSTTL = listNSTTL;
    }

    public int getNumNameServer() {
        return numNameServer;
    }

    public void setNumNameServer(int numNameServer) {
        this.numNameServer = numNameServer;
    }

    public float getDistributionNSAS() {
        return distributionNSAS;
    }

    public void setDistributionNSAS(float distributionNSAS) {
        this.distributionNSAS = distributionNSAS;
    }

    public Float[] getHitASRatio() {
        return hitASRatio;
    }

    public void setHitASRatio(Float[] hitASRatio) {
        this.hitASRatio = hitASRatio;
    }

    public DNS_Feature() {
        popularTLDRatio = new Float[6];         // com, org, edu, gov, uk, rest
        hitASRatio = new Float[3];            // malware, phishing, spamming
        distributionNSAS = 0;
        numNameServer = 0;
        listNSTTL = new ArrayList<Integer>();
        listDNSRecordTTL = new ArrayList<Integer>();
    }
}
