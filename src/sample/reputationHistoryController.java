package sample;

import data_structure.instance_ML.historySitesReputation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by steve on 30/05/2016.
 */
public class reputationHistoryController implements Initializable {
    public TableView historyReputationTableView = new TableView();

    public void handleBackButton(ActionEvent actionEvent) {
        // Back into main page
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("css/main_page.css").toExternalForm());
        stage.setScene(scene);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Define columns tableview
        TableColumn nameColumn = new TableColumn("Domain Name");
        nameColumn.setMinWidth(100);
        nameColumn.setCellValueFactory(
                new PropertyValueFactory<historyReputationRowTableView,String>("domainName"));

        TableColumn labelColumn = new TableColumn("Status");
        nameColumn.setMinWidth(100);
        nameColumn.setCellValueFactory(
                new PropertyValueFactory<historyReputationRowTableView,String>("domainStatus"));

        TableColumn responseTimeColumn = new TableColumn("Response Time");
        nameColumn.setMinWidth(100);
        nameColumn.setCellValueFactory(
                new PropertyValueFactory<historyReputationRowTableView,String>("domainResponseTime"));

        TableColumn dateMeasuredColumn = new TableColumn("Date Measured");
        nameColumn.setMinWidth(100);
        nameColumn.setCellValueFactory(
                new PropertyValueFactory<historyReputationRowTableView,String>("domainDateMeasured"));

        TableColumn malwareCompositionColumn = new TableColumn("Malware Probability");
        nameColumn.setMinWidth(100);
        nameColumn.setCellValueFactory(
                new PropertyValueFactory<historyReputationRowTableView,String>("domainMalComp"));

        TableColumn phishingCompositionColumn = new TableColumn("Phishing Probability");
        nameColumn.setMinWidth(100);
        nameColumn.setCellValueFactory(
                new PropertyValueFactory<historyReputationRowTableView,String>("domainPhisComp"));

        TableColumn spammingCompositionColumn = new TableColumn("Spamming Probability");
        nameColumn.setMinWidth(100);
        nameColumn.setCellValueFactory(
                new PropertyValueFactory<historyReputationRowTableView,String>("domainSpamComp"));

        historyReputationTableView.getColumns().addAll(nameColumn,labelColumn,responseTimeColumn,
                malwareCompositionColumn,phishingCompositionColumn,spammingCompositionColumn);

        // Insert history reputation data into tableview
        ObservableList <historyReputationRowTableView> data = FXCollections.observableArrayList();
        List<Pair<String,historySitesReputation>> listHistoryReputation = reputationResultController.loadHistoryReputation();
        for (int i=0;i<listHistoryReputation.size();i++) {
            Pair<String,historySitesReputation> reputationData = listHistoryReputation.get(i);
            String domainName = reputationData.getValue0();
            historySitesReputation domainReputation = reputationData.getValue1();
            String domainStatus = domainReputation.getLabelNormality();
            String domainResponseTime = String.valueOf(domainReputation.getResponseTime());
            String domainDateMeasured = String.valueOf(domainReputation.getMeasureDate());
            Triplet<Double,Double,Double> domainComposition = domainReputation.getCompositionDangerousity();
            String domainMalComp = String.valueOf(domainComposition.getValue0());
            String domainPhisComp = String.valueOf(domainComposition.getValue1());
            String domainSpamComp = String.valueOf(domainComposition.getValue2());
            data.add(new historyReputationRowTableView
                    (domainName,domainStatus,domainResponseTime,domainDateMeasured,domainMalComp,domainPhisComp,domainSpamComp));
        }
        historyReputationTableView.setItems(data);
    }

    public static class historyReputationRowTableView {
        private final SimpleStringProperty domainName;
        private final SimpleStringProperty domainStatus;
        private final SimpleStringProperty domainResponseTime;
        private final SimpleStringProperty domainDateMeasured;
        private final SimpleStringProperty domainMalComp;
        private final SimpleStringProperty domainPhisComp;
        private final SimpleStringProperty domainSpamComp;

        public historyReputationRowTableView(String domainName, String domainStatus, String domainResponseTime, String domainDateMeasured, String domainMalComp, String domainPhisComp, String domainSpamComp) {
            this.domainName = new SimpleStringProperty(domainName);
            this.domainStatus = new SimpleStringProperty(domainStatus);
            this.domainResponseTime = new SimpleStringProperty(domainResponseTime);
            this.domainDateMeasured = new SimpleStringProperty(domainDateMeasured);
            this.domainMalComp = new SimpleStringProperty(domainMalComp);
            this.domainPhisComp = new SimpleStringProperty(domainPhisComp);
            this.domainSpamComp = new SimpleStringProperty(domainSpamComp);
        }

        public String getDomainSpamComp() {
            return domainSpamComp.get();
        }

        public SimpleStringProperty domainSpamCompProperty() {
            return domainSpamComp;
        }

        public void setDomainSpamComp(String domainSpamComp) {
            this.domainSpamComp.set(domainSpamComp);
        }

        public String getDomainPhisComp() {
            return domainPhisComp.get();
        }

        public SimpleStringProperty domainPhisCompProperty() {
            return domainPhisComp;
        }

        public void setDomainPhisComp(String domainPhisComp) {
            this.domainPhisComp.set(domainPhisComp);
        }

        public String getDomainMalComp() {
            return domainMalComp.get();
        }

        public SimpleStringProperty domainMalCompProperty() {
            return domainMalComp;
        }

        public void setDomainMalComp(String domainMalComp) {
            this.domainMalComp.set(domainMalComp);
        }

        public String getDomainDateMeasured() {
            return domainDateMeasured.get();
        }

        public SimpleStringProperty domainDateMeasuredProperty() {
            return domainDateMeasured;
        }

        public void setDomainDateMeasured(String domainDateMeasured) {
            this.domainDateMeasured.set(domainDateMeasured);
        }

        public String getDomainResponseTime() {
            return domainResponseTime.get();
        }

        public SimpleStringProperty domainResponseTimeProperty() {
            return domainResponseTime;
        }

        public void setDomainResponseTime(String domainResponseTime) {
            this.domainResponseTime.set(domainResponseTime);
        }

        public String getDomainStatus() {
            return domainStatus.get();
        }

        public SimpleStringProperty domainStatusProperty() {
            return domainStatus;
        }

        public void setDomainStatus(String domainStatus) {
            this.domainStatus.set(domainStatus);
        }

        public String getDomainName() {
            return domainName.get();
        }

        public SimpleStringProperty domainNameProperty() {
            return domainName;
        }

        public void setDomainName(String domainName) {
            this.domainName.set(domainName);
        }
    }
}