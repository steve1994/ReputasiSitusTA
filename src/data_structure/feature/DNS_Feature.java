package data_structure.feature;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 27/03/2016.
 */
public class DNS_Feature {
    private Double[] popularTLDRatio;
    private Double[] hitASRatio;
    private double distributionNSAS;
    private int numNameServer;
    private Integer listNSTTL;
    private Integer listDNSRecordTTL;

    public Double[] getPopularTLDRatio() {
        return popularTLDRatio;
    }

    public void setPopularTLDRatio(Double[] popularTLDRatio) {
        this.popularTLDRatio = popularTLDRatio;
    }

    public Integer getListDNSRecordTTL() {
        return listDNSRecordTTL;
    }

    public void setListDNSRecordTTL(Integer listDNSRecordTTL) {
        this.listDNSRecordTTL = listDNSRecordTTL;
    }

    public Integer getListNSTTL() {
        return listNSTTL;
    }

    public void setListNSTTL(Integer listNSTTL) {
        this.listNSTTL = listNSTTL;
    }

    public int getNumNameServer() {
        return numNameServer;
    }

    public void setNumNameServer(int numNameServer) {
        this.numNameServer = numNameServer;
    }

    public double getDistributionNSAS() {
        return distributionNSAS;
    }

    public void setDistributionNSAS(double distributionNSAS) {
        this.distributionNSAS = distributionNSAS;
    }

    public Double[] getHitASRatio() {
        return hitASRatio;
    }

    public void setHitASRatio(Double[] hitASRatio) {
        this.hitASRatio = hitASRatio;
    }

    public DNS_Feature() {
        popularTLDRatio = new Double[4];         // com, org, uk, rest
        hitASRatio = new Double[3];            // malware, phishing, spamming
        distributionNSAS = 0.0;
        numNameServer = 0;
//        listNSTTL = new ArrayList<Integer>();
//        listDNSRecordTTL = new ArrayList<Integer>();
    }
}
