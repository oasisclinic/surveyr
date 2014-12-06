package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.wordnik.swagger.annotations.*;
import errors.EmptyResponseBodyException;
import errors.NoObjectsFoundError;
import models.dto.SurveyDTO;
import play.libs.F.Promise;
import play.libs.ws.WSRequestHolder;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Qualtrics;
import utilities.Rest;
import utilities.Secure;

import javax.ws.rs.Produces;
import java.util.*;

import static play.mvc.Results.redirect;

/**
 * Handles operations involving survey definitions from Qualtrics.
 * @author Bradley Davis
 */
@Security.Authenticated(Secure.class)
@Api(value = "/api/surveys", description = "Handles requests involving surveys (definitions) from Qualtrics")
public class SurveyController {

    /**
     * Gets a list of surveys available from Qualtrics.
     * @return a DTO containing survey name and ID
     */
    @ApiOperation(nickname = "findAll", value = "Get a list of currently available surveys", httpMethod = "GET", response = SurveyDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Surveys found"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "No surveys found")
    })
    @Produces("application/json")
    public static Promise<Result> findAll() {

        // Get surveys
        WSRequestHolder surveysRequest = Qualtrics.request("getSurveys")
                .setQueryParameter("Format", "JSON");

        // Then
        return surveysRequest.get().map(response -> {

                    // For each survey, grab the survey name and ID and add it to a DTO
                    List<SurveyDTO> surveys = new LinkedList<>();
                    JsonNode node = response.asJson().with("Result").withArray("Surveys");
                    Iterator<JsonNode> it = node.elements();
                    while (it.hasNext()) {
                        JsonNode n = it.next();
                        surveys.add(new SurveyDTO(n.get("SurveyID").asText(), n.get("SurveyName").asText()));
                    }

                    try {
                        return Rest.json(surveys);
                    } catch (EmptyResponseBodyException e) {
                        return Rest.error(new NoObjectsFoundError("surveys"));
                    }

                }
        );

    }

}