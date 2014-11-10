package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.wordnik.swagger.annotations.*;
import models.QualtricsAPI;
import models.SurveyDTO;
import play.libs.F;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import play.mvc.Result;

import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static controllers.BaseApiController.JsonResponse;
import static play.mvc.Results.redirect;

@Api(value = "/api/surveys", description = "Operations involving surveys")
public class SurveyApiController {

    @ApiOperation(nickname="findAll", value = "Get a list of available surveys", httpMethod = "GET", response = SurveyDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Surveys found"),
            @ApiResponse(code = 404, message = "No surveys found")
    })
    @Produces("application/json")
    public static F.Promise<Result> findAll() {

        WSRequestHolder surveys = QualtricsAPI.request("getSurveys")
                .setQueryParameter("Format", "JSON");

        F.Promise<Result> surveysPromise = surveys.get().map(
                new F.Function<WSResponse, Result>() {
                    public Result apply(WSResponse response) {
                        List<SurveyDTO> surveys = new LinkedList<>();
                        JsonNode node = response.asJson().with("Result").withArray("Surveys");
                        Iterator<JsonNode> it = node.elements();
                        while (it.hasNext()) {
                            JsonNode n = it.next();
                            surveys.add(new SurveyDTO(n.get("SurveyID").asText(), n.get("SurveyName").asText()));
                        }
                        if (surveys.size() > 0) {
                            return JsonResponse(200, surveys);
                        } else {
                            return JsonResponse(404, surveys);
                        }
                    }
                }
        );

        return surveysPromise;

    }

    @ApiOperation(nickname = "administerSurvey", value = "Administer a survey", httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 302, message = "Administration started")
    })
    public static Result administerSurvey(@ApiParam(name = "surveyId", value = "the Qualtrics survey ID to administer", required = true)
                                          @PathParam("surveyId") String surveyId,
                                          @ApiParam(name = "patientId", value = "the ID of the patient to administer a survey to", required = true)
                                          @PathParam("patientId") String patientId) {

        return redirect(QualtricsAPI.createSurveyUrl(surveyId, patientId));

    }


}
