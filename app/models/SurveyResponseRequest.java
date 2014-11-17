package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.jongo.Find;
import org.jongo.MongoCollection;
import play.data.validation.Constraints;
import uk.co.panaxiom.playjongo.PlayJongo;
import utilities.MongoList;

import java.util.*;

public class SurveyResponseRequest {

    @JsonIgnore
    private static final MongoCollection collection = PlayJongo.getCollection("responseRequests");

    public ObjectId _id;

    @ApiModelProperty(value = "a unique request ID", required = false)
    private String requestId;

    @Constraints.Required
    @ApiModelProperty(value = "patient ID", required = true)
    private String patientId;

    @Constraints.Required
    @ApiModelProperty(value = "the ID of the Qualtrics survey in progress", required = true)
    private String surveyId;

    @Constraints.Required
    @ApiModelProperty(value = "the name of the Qualtrics survey in progress", required = true)
    private String surveyName;

    @ApiModelProperty(value = "the Qualtrics-provided response ID", required = false)
    private String responseId;

    @Constraints.Required
    @ApiModelProperty(value = "indicates whether the request has been completed", required = true)
    private boolean complete;

    @Constraints.Required
    @ApiModelProperty(value = "the date the response request was made", required = true)
    private Date startDate;

    public SurveyResponseRequest(){}

    public SurveyResponseRequest(String requestId, String patientId, String surveyId, String surveyName, Date startDate) {
        this.requestId = requestId;
        this.patientId = patientId;
        this.surveyId = surveyId;
        this.surveyName = surveyName;
        this.startDate = startDate;
    }

    public SurveyResponseRequest save() {
        collection.save(this);
        return this;
    }

    public static List<SurveyResponseRequest> find(Boolean complete, Integer limit) {
        String query = (complete != null) ? "{complete: " + complete + "}" : "";
        Find f = collection.find(query);
        if (limit != null) f = f.limit(limit).sort("{startDate: -1}");
        return new MongoList<SurveyResponseRequest>(f, SurveyResponseRequest.class).getList();
    }

    public static List<SurveyResponseRequest> findByPatientId(String patientId, Boolean complete, Integer limit) {
        String query = (complete != null) ? "complete: " + complete : "";
        Find f = collection.find("{patientId: #, " + query + "}", patientId);
        if (limit != null) f = f.limit(limit).sort("{startDate: -1}");
        return new MongoList<SurveyResponseRequest>(f, SurveyResponseRequest.class).getList();
    }

    public static SurveyResponseRequest findOne(String requestId) {
        return collection.findOne("{requestId: #}", requestId).as(SurveyResponseRequest.class);
    }

    public SurveyResponseRequest markComplete() {
        this.complete = true;
        collection.update("{_id: #}", this._id).with("{$set: {complete: #}}", complete);
        return this;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId.toString();
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getSurveyName() {
        return surveyName;
    }

    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @JsonIgnore
    public ObjectId get_id() {
        return _id;
    }

    @JsonProperty("_id")
    public void set_id(ObjectId _id) {
        this._id = _id;
    }
}
