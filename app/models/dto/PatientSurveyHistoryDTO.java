package models.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO for graph data
 * @author Bradley Davis
 */
public class PatientSurveyHistoryDTO {

    private Map<String, String> definition;
    private List<SurveyDataDTO> data = new ArrayList<>();

    public void addData(SurveyDataDTO data) {
        this.data.add(data);
    }

    public void setData(List<SurveyDataDTO> data) {
        this.data = data;
    }

    public void setDefinition(Map<String, String> definition) {
        this.definition = definition;
    }

    public Map<String, String> getDefinition() {
        return definition;
    }

    public List<SurveyDataDTO> getData() {
        return data;
    }

}
