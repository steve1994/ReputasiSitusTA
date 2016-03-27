package data_structure.instance_ML;

import data_structure.feature.DNS_Feature;
import data_structure.feature.Spesific_Feature;
import data_structure.feature.Trust_Feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by steve on 26/03/2016.
 */
public class SiteRecordReputation {
    private DNS_Feature DNSRecordFeature;
    private Spesific_Feature SpesificRecordFeature;
    private Trust_Feature TrustRecordFeature;

    public DNS_Feature getDNSRecordFeature() {
        return DNSRecordFeature;
    }

    public void setDNSRecordFeature(DNS_Feature DNSRecordFeature) {
        this.DNSRecordFeature = DNSRecordFeature;
    }

    public Spesific_Feature getSpesificRecordFeature() {
        return SpesificRecordFeature;
    }

    public void setSpesificRecordFeature(Spesific_Feature spesificRecordFeature) {
        SpesificRecordFeature = spesificRecordFeature;
    }

    public Trust_Feature getTrustRecordFeature() {
        return TrustRecordFeature;
    }

    public void setTrustRecordFeature(Trust_Feature trustRecordFeature) {
        TrustRecordFeature = trustRecordFeature;
    }

    public SiteRecordReputation(int typeReputation) {
        DNSRecordFeature = new DNS_Feature();
        SpesificRecordFeature = new Spesific_Feature();
        TrustRecordFeature = new Trust_Feature();
    }
}
