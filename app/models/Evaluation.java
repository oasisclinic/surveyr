package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import uk.co.panaxiom.playjongo.PlayJongo;
import utilities.MongoList;

import java.util.*;

public class Evaluation {

    private static final MongoCollection collection = PlayJongo.getCollection("evaluations");
    private static final List<String> personalAttributes = Arrays.asList(new String[]{"IPAddress", "EmailAddress", "Name", "requestId", "ExternalDataReference", "ResponseSet"});
    private static final String query = "{%s : #}";

    public ObjectId _id;
    private String evaluationId;
    private String patientId;
    private String surveyId;
    private String surveyName;
    private int pinCode;
    private String responseId;
    private boolean complete;
    private Date startDate;
    private Date endDate;
    private JsonNode data;

    public Evaluation(){}

    public Evaluation(String patientId, String surveyId, String surveyName) {
        this.evaluationId = UUID.randomUUID().toString();
        this.patientId = patientId;
        this.surveyId = surveyId;
        this.surveyName = surveyName;
        this.startDate = new Date();
        this.pinCode = new PinCode().generate().save().getPinCode();
    }

    public Evaluation complete(String responseId, JsonNode data){
        this.responseId = responseId;
        this.endDate = new Date();
        this.data = stripPersonalInfo(responseId, data);
        this.complete = true;
        PinCode.remove(this.getPinCode());
        return this;
    }

    private static ObjectNode stripPersonalInfo(String responseId, JsonNode n) {
        return ((ObjectNode) n.get(responseId)).remove(personalAttributes);
    }

    public Evaluation save() {
        collection.save(this);
        return this;
    }

    public Evaluation remove() {
        collection.remove("{evaluationId: '" + this.evaluationId + "'}");
        return this;
    }

    public static Evaluation findOneByField(String fieldName, Object value) {
        return collection.findOne(String.format(query, fieldName), value).as(Evaluation.class);
    }

    public static List<Evaluation> findByField(String fieldName, Object value) {
        return new MongoList<Evaluation>(collection.find(String.format(query, fieldName), value).sort("{startDate: -1}"), Evaluation.class).getList();
    }

    public static List<Evaluation> findByPatientIdSurveyId(String patientId, String surveyId) {
        return new MongoList<Evaluation>(collection.find("{surveyId: #, patientId: #, complete: true}", surveyId, patientId).sort("{endDate: 1}"), Evaluation.class).getList();
    }

    public ObjectId get_id() {
        return _id;
    }

    public String getEvaluationId() {
        return evaluationId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getSurveyId() {
        return surveyId;
    }

    public String getSurveyName() {
        return surveyName;
    }

    public int getPinCode() {
        return pinCode;
    }

    public String getResponseId() {
        return responseId;
    }

    public boolean isComplete() {
        return complete;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public JsonNode getData() {
        return data;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setData(ObjectNode data) {
        this.data = data;
    }

}
