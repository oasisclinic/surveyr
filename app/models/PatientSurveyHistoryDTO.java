package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author:
 * Bradley H Davis
 * 720326365
 * The University of North Carolina at Chapel Hill
 * Created on 10/30/14 at 1:12 PM.
 */
public class PatientSurveyHistoryDTO {

    private String surveyId;
    private String patientId;
    private Map<String, String> definition;
    private List<SurveyDataDTO> data;

    public PatientSurveyHistoryDTO(String surveyId, String patientId) {
        this.surveyId = surveyId;
        this.patientId = patientId;
        this.data = new ArrayList<>();
    }

    public void addData(SurveyDataDTO data) {
        this.data.add(data);
    }

    public void setData(List<SurveyDataDTO> data) {
        this.data = data;
    }

    public void setDefinition(Map<String, String> definition) {
        this.definition = definition;
    }

    public String getSurveyId() {
        return surveyId;
    }

    public String getPatientId() {
        return patientId;
    }

    public Map<String, String> getDefinition() {
        return definition;
    }

    public List<SurveyDataDTO> getData() {
        return data;
    }

}
