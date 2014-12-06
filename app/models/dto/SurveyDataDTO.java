package models.dto;

import java.util.Date;
import java.util.HashMap;

/**
 * DTO for one response to a survey
 * @author Bradley Davis
 */
public class SurveyDataDTO {

    private Date date;
    private HashMap<String, Integer> data;

    public SurveyDataDTO(Date date) {
        this.date = date;
        this.data = new HashMap<>();
    }

    public SurveyDataDTO(Date date, HashMap<String, Integer> data) {
        this.date = date;
        this.data = data;
    }

    public void addData(String key, Integer value) {
        data.put(key, value);
    }

    public HashMap<String, Integer> getData() {
        return data;
    }

    public Date getDate() {
        return date;
    }

}
