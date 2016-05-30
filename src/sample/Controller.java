package sample;

import Utils.Database.EksternalFile;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public RadioButton supervisedRadioButton;
    public RadioButton unsupervisedRadioButton;
    public RadioButton hybridRadioButton;
    public RadioButton trustRadioButton;
    public RadioButton spesificRadioButton;
    public RadioButton dnsRadioButton;
    public RadioButton dnsSpesificRadioButton;
    public RadioButton dnsTrustRadioButton;
    public RadioButton spesificTrustRadioButton;
    public RadioButton dnsSpesificTrustRadioButton;
    private final ToggleGroup methodRadioButton;
    private final ToggleGroup featuresRadioButton;

    public Controller() {
        methodRadioButton = new ToggleGroup();
        featuresRadioButton = new ToggleGroup();
    }


//    @FXML private RadioButton malwareRadioButton;
//    @FXML private RadioButton phishingRadioButton;
//    @FXML private RadioButton spammingRadioButton;
//    @FXML private RadioButton popularRadioButton;
//    @FXML private TextField numSitesTextField;
//    @FXML private CheckBox optionDNSCheckBox;
//    @FXML private CheckBox optionSpesificCheckBox;
//    @FXML private CheckBox optionTrustCheckBox;
//    private final ToggleGroup siteListRadioButton;

//    public void handleTrainingDataFeatures(ActionEvent actionEvent) {
//        // Setting static vars
//        StaticVars.numSitesTraining = Integer.parseInt(numSitesTextField.getText());
//        if (phishingRadioButton.isSelected()) {
//            StaticVars.listSitesTraining = EksternalFile.loadSitesTrainingList(2).getKey();
//        } else if (spammingRadioButton.isSelected()) {
//            StaticVars.listSitesTraining = EksternalFile.loadSitesTrainingList(3).getKey();
//        } else if (popularRadioButton.isSelected()) {
//            StaticVars.listSitesTraining = EksternalFile.loadSitesTrainingList(4).getKey();
//        } else {
//            StaticVars.listSitesTraining = EksternalFile.loadSitesTrainingList(1).getKey();
//        }
//        if (optionDNSCheckBox.isSelected() && (!optionSpesificCheckBox.isSelected()) && (!optionTrustCheckBox.isSelected())) {
//            StaticVars.reputationType = 1;
//        } else if (!optionDNSCheckBox.isSelected() && (optionSpesificCheckBox.isSelected()) && (!optionTrustCheckBox.isSelected())) {
//            StaticVars.reputationType = 2;
//        } else if (!optionDNSCheckBox.isSelected() && (!optionSpesificCheckBox.isSelected()) && (optionTrustCheckBox.isSelected())) {
//            StaticVars.reputationType = 3;
//        } else if (optionDNSCheckBox.isSelected() && (optionSpesificCheckBox.isSelected()) && (!optionTrustCheckBox.isSelected())) {
//            StaticVars.reputationType = 4;
//        } else if (optionDNSCheckBox.isSelected() && (!optionSpesificCheckBox.isSelected()) && (optionTrustCheckBox.isSelected())) {
//            StaticVars.reputationType = 5;
//        } else if (optionDNSCheckBox.isSelected() && (optionSpesificCheckBox.isSelected()) && (!optionTrustCheckBox.isSelected())) {
//            StaticVars.reputationType = 6;
//        } else {
//            StaticVars.reputationType = 7;
//        }
//
//        // Go into Statistic screens
//        Parent root = null;
//        try {
//            root = FXMLLoader.load(getClass().getResource("statistik.fxml"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
//        stage.setScene(new Scene(root));
//    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        malwareRadioButton.setToggleGroup(siteListRadioButton);
//        phishingRadioButton.setToggleGroup(siteListRadioButton);
//        spammingRadioButton.setToggleGroup(siteListRadioButton);
//        popularRadioButton.setToggleGroup(siteListRadioButton);
//        malwareRadioButton.setSelected(true);
        
    }

    public void handleDiagnoseButton(ActionEvent actionEvent) {
        
    }

    public void handleHistoryButton(ActionEvent actionEvent) {

    }
}
