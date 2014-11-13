package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.jongo.*;
import play.Logger;
import uk.co.panaxiom.playjongo.PlayJongo;
import utilities.MongoList;

import java.util.*;

public class SurveyResponse {

    private ObjectId _id;

    private String patientId;

    private String surveyId;

    private JsonNode data;

    private static MongoCollection collection(String surveyId) {
        return PlayJongo.getCollection(surveyId);
    }

    public SurveyResponse save() {
        collection(this.surveyId).save(this);
        return this;
    }

    public static List<SurveyResponse> findByPatientId(String surveyId, String patientId) {
        return new MongoList<SurveyResponse>(collection(surveyId).find("{patientId: #}", patientId), SurveyResponse.class).getList();
    }

    @JsonIgnore
    public ObjectId get_id() {
        return _id;
    }

    @JsonProperty("_id")
    public void set_id(ObjectId _id) {
        this._id = _id;
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

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }
}
