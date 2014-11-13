package models.dto;

import com.wordnik.swagger.annotations.ApiModel;

@ApiModel(value = "A survey is a container that corresponds to a Qualtrics survey")
public class SurveyDTO {

    public String id;
    public String name;

    public SurveyDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }

}
