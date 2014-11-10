package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import play.data.validation.Constraints;
import uk.co.panaxiom.playjongo.PlayJongo;

import java.util.*;

public class SurveyResponseRequest {

    @JsonIgnore
    private static final MongoCollection collection = PlayJongo.getCollection("responseRequests");

    @JsonProperty("_id")
    public ObjectId id;

    @ApiModelProperty(value = "a unique request ID", required = false)
    private String requestId;

    @Constraints.Required
    @ApiModelProperty(value = "patient ID", required = true)
    private String patientId;

    @Constraints.Required
    @ApiModelProperty(value = "the ID of the Qualtrics survey in progress", required = true)
    private String surveyId;

    @ApiModelProperty(value = "the Qualtrics-provided response ID", required = false)
    private String responseId;

    @Constraints.Required
    @ApiModelProperty(value = "indicates whether the request has been completed", required = true)
    private boolean complete;

    public SurveyResponseRequest save() {
        collection.save(this);
        return this;
    }

    public static List<SurveyResponseRequest> findIncomplete() {
        List<SurveyResponseRequest> requests = new LinkedList<>();
        Iterator<SurveyResponseRequest> it = collection.find("{complete: #}", false).as(SurveyResponseRequest.class).iterator();
        while(it.hasNext()) requests.add(it.next());
        return requests;
    }

    public static List<SurveyResponseRequest> findAll() {
        List<SurveyResponseRequest> requests = new LinkedList<>();
        Iterator<SurveyResponseRequest> it = collection.find().as(SurveyResponseRequest.class).iterator();
        while(it.hasNext()) requests.add(it.next());
        return requests;
    }

    public static SurveyResponseRequest findOne(String requestId) {
        return collection.findOne("{requestId: #}", requestId).as(SurveyResponseRequest.class);
    }

    public SurveyResponseRequest markComplete() {
        this.complete = true;
        collection.update("{_id: #}", this.id).with("{$set: {complete: #}}", complete);
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

}
