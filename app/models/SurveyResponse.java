package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.jongo.*;
import play.Logger;
import uk.co.panaxiom.playjongo.PlayJongo;

import java.util.*;

public class SurveyResponse {

    @JsonProperty("_id")
    public ObjectId id;

    public String patientId;

    public String surveyId;

    public JsonNode data;

    public SurveyResponse() {

    }

    private static MongoCollection results(String surveyId) {
        return PlayJongo.getCollection(surveyId);
    }

    public SurveyResponse insert() {
        results(this.surveyId).save(this);
        return this;
    }

    public static List<SurveyResponse> findByPatientId(String surveyId, String patientId) {

        ArrayList<SurveyResponse> results = new ArrayList<>();
        Iterable<SurveyResponse> t = results(surveyId).find("{patientId: #}", patientId).as(SurveyResponse.class);
        Iterator i = t.iterator();
        while (i.hasNext()) {
            results.add((SurveyResponse) i.next());
        }
        return results;
    }

}
