package sample;

import data_structure.instance_ML.SiteRecordReputation;
import data_structure.instance_ML.historySitesReputation;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by steve on 08/04/2016.
 */
public class StaticVars {
    public static List<String> listSitesTraining;
    public static int numSitesTraining;
    public static int reputationType;
    public static int methodType;
    public static HashMap<String,historySitesReputation> historyReputation = new HashMap<String, historySitesReputation>();
    public static String currentDomainName;
    public static String currentLabel;
    public static Triplet<Double,Double,Double> currentComposition;
}
