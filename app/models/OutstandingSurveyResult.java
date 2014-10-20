package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import uk.co.panaxiom.playjongo.PlayJongo;

import java.util.UUID;

public class OutstandingSurveyResult {

    @JsonProperty("_id")
    public ObjectId id;

    private String requestId;

    private String medicalId;

    private String surveyId;

    private String responseSetId;

    private static MongoCollection results() {
        return PlayJongo.getCollection("outstanding");
    }

    public OutstandingSurveyResult insert() {
        results().save(this);
        return this;
    }

    public void remove() {
        results().remove(this.id);
    }

    public static OutstandingSurveyResult findByRequestId(String requestId) {
        return results().findOne("{requestId: #}", requestId).as(OutstandingSurveyResult.class);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMedicalId() {
        return medicalId;
    }

    public void setMedicalId(String medicalId) {
        this.medicalId = medicalId;
    }

    public String getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }

    public String getResponseSetId() {
        return responseSetId;
    }

    public void setResponseSetId(String responseSetId) {
        this.responseSetId = responseSetId;
        results().update("{_id: #}", this.id).with("{$set: {responseSetId: #}}", responseSetId);
    }

}
