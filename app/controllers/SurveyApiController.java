package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.wordnik.swagger.annotations.*;
import models.*;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;

import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static controllers.BaseApiController.JsonResponse;
import static play.mvc.Results.ok;
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

        UUID requestId = UUID.randomUUID();

        SurveyResponseRequest request = new SurveyResponseRequest();
        request.setPatientId(patientId);
        request.setSurveyId(surveyId);
        request.setRequestId(requestId);
        request.save();

        return redirect(QualtricsAPI.createSurveyUrl(surveyId, requestId.toString()));

    }

    @ApiOperation(nickname = "completeSurvey", value = "Complete a survey administration", httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Survey response recorded")
    })
    public static F.Promise<Result> completeSurvey(@ApiParam(name = "requestId", value = "the ID of the survey request", required = true)
                                          @PathParam("requestId") String requestId,
                                          @ApiParam(name = "responseId", value = "the Qualtrics-provided response ID", required = true)
                                          @PathParam("responseId") String responseId) {

        SurveyResponseRequest request = SurveyResponseRequest.findOne(requestId);
        request.setResponseId(responseId);
        request.setComplete(true);
        request.save();

        WSRequestHolder surveyAnswers = QualtricsAPI.request("getLegacyResponseData")
                .setQueryParameter("Format", "JSON")
                .setQueryParameter("SurveyID", request.getSurveyId())
                .setQueryParameter("ResponseID", responseId)
                .setQueryParameter("ExportQuestionIDs", "1");

        SurveyResponse response = new SurveyResponse();
        response.patientId = request.getPatientId();
        response.surveyId = request.getSurveyId();

        F.Promise<Result> responsePromise = surveyAnswers.get().map(
                new F.Function<WSResponse, Result>() {
                    public Result apply(WSResponse w) {
                        response.data = w.asJson();
                        response.insert();
                        return JsonResponse(200, response);
                    }
                }
        );

        return responsePromise;

    }

    @ApiOperation(nickname="findAllResponseRequests", value = "Get all survey response requests", httpMethod = "GET", responseContainer = "List", response = SurveyResponseRequest.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Requests found"),
            @ApiResponse(code = 404, message = "No requests found")
    })
    @Produces("application/json")
    public static Result findAllResponseRequests() {

        List<SurveyResponseRequest> requests = SurveyResponseRequest.findAll();

        if (requests.size() > 0) {
            return JsonResponse(200, requests);
        } else {
            return JsonResponse(404, requests);
        }

    }

    @ApiOperation(nickname="findAllIncompleteResponseRequests", value = "Get all survey response requests that have yet to be completed", httpMethod = "GET", responseContainer = "List", response = SurveyResponseRequest.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Incomplete requests found"),
            @ApiResponse(code = 404, message = "No incomplete requests found")
    })
    @Produces("application/json")
    public static Result findAllIncompleteResponseRequests() {

        List<SurveyResponseRequest> requests = SurveyResponseRequest.findIncomplete();

        if (requests.size() > 0) {
            return JsonResponse(200, requests);
        } else {
            return JsonResponse(404, requests);
        }

    }


}
