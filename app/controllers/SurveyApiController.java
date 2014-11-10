package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.wordnik.swagger.annotations.*;
import models.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.XML;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;

import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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

    @ApiOperation(nickname="findByPatientId", value = "Retrieve all responses for a single user and survey", httpMethod = "GET", response = PatientSurveyHistoryDTO.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Surveys found")})
    public static F.Promise<Result> findByPatientId(@ApiParam(name = "surveyId", value = "the ID of the survey", required = true)
                                                        @PathParam("surveyId") String surveyId,
                                                    @ApiParam(name = "patientId", value = "the ID of the patient", required = true)
                                                    @PathParam("patientId") String patientId) {

        WSRequestHolder surveyDef = QualtricsAPI.request("getSurvey").setQueryParameter("SurveyID", surveyId);

        List<SurveyResponse> responses = SurveyResponse.findByPatientId(surveyId, patientId);


        F.Promise<Result> results = F.Promise.sequence(surveyDef.get()).map(
                list -> {

                    Document d = XML.fromString(list.get(0).getBody());

                    PatientSurveyHistoryDTO psh = new PatientSurveyHistoryDTO(surveyId, patientId);

                    //Logger.debug("responses found: " + responses.size());

                    for (SurveyResponse r : responses) {

                        String testDate = r.data.findValuesAsText("EndDate").get(0);
                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = formatter.parse(testDate);

                        SurveyDataDTO dataDTO = new SurveyDataDTO(date);

                        HashMap<String, String> q = new HashMap<>();

                        //Logger.info(Json.toJson(r.data).toString());

                        Iterator<Map.Entry<String, JsonNode>> it = r.data.get(r.data.fieldNames().next()).fields();
                        int i = 0;
                        while(it.hasNext()) {

                            Map.Entry<String, JsonNode> o = it.next();
                            if (o.getKey().matches("[Q][I][D]\\d") || o.getKey().matches("[Q][I][D]\\d[_]\\d")) {

                                dataDTO.addData(String.valueOf(i), o.getValue().asInt());
                                Logger.debug("added " + i + ", " + o.getValue().asInt());

                                NodeList l = d.getElementsByTagName("Questions").item(0).getChildNodes();
                                for (int z = 0; z < l.getLength(); z++) {

                                    Node node = l.item(z);
                                    Element e = (Element) node;
                                    if (o.getKey().contains(e.getAttribute("QuestionID"))) {

                                        q.put(String.valueOf(i), e.getElementsByTagName("QuestionDescription").item(0).getTextContent());

                                    }

                                }

                                i++;
                            }


                        }

                        psh.setDefinition(q);
                        psh.addData(dataDTO);

                    }

                    return JsonResponse(psh);
                }
        );

        return results;

    }


}
