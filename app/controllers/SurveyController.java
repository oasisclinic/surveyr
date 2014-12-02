package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordnik.swagger.annotations.*;
import errors.EmptyResponseBodyException;
import errors.NoObjectsFoundError;
import models.Patient;
import models.SurveyResponse;
import models.SurveyResponseRequest;
import models.dto.PatientSurveyHistoryDTO;
import models.dto.SurveyDTO;
import models.dto.SurveyDataDTO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.libs.F.Option;
import play.libs.F.Promise;
import play.libs.XML;
import play.libs.ws.WSRequestHolder;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Qualtrics;
import utilities.Rest;
import utilities.Secure;

import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static play.mvc.Results.redirect;

@Security.Authenticated(Secure.class)
@Api(value = "/api/surveys", description = "Operations involving surveys")
public class SurveyController {

    @ApiOperation(nickname = "findAll", value = "Get a list of available surveys", httpMethod = "GET", response = SurveyDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Surveys found"),
            @ApiResponse(code = 404, message = "No surveys found")
    })
    @Produces("application/json")
    public static Promise<Result> findAll() {

        WSRequestHolder surveysRequest = Qualtrics.request("getSurveys")
                .setQueryParameter("Format", "JSON");

        return surveysRequest.get().map(response -> {

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

    @ApiOperation(nickname = "administerSurvey", value = "Administer a survey", httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 302, message = "Administration started")
    })
    public static Promise<Result> administerSurvey(@ApiParam(name = "surveyId", value = "the Qualtrics survey ID to administer", required = true)
                                          @PathParam("surveyId") String surveyId,
                                          @ApiParam(name = "patientId", value = "the ID of the patient to administer a survey to", required = true)
                                          @PathParam("patientId") String patientId) {

        WSRequestHolder getSurveyName = Qualtrics.request("getSurveyName")
                .setQueryParameter("Format", "JSON")
                .setQueryParameter("SurveyID", surveyId);

        return getSurveyName.get().map(response -> {
                    String surveyName = response.asJson().with("Result").get("SurveyName").asText();
                    String requestId = UUID.randomUUID().toString();
                    SurveyResponseRequest request = new SurveyResponseRequest(requestId, patientId, surveyId, surveyName, new Date()).save();
                    return redirect(Qualtrics.createSurveyUrl(surveyId, requestId));
                }
        );

    }

    @ApiOperation(nickname = "completeSurvey", value = "Complete a survey administration", httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Survey response recorded")
    })
    public static Promise<Result> completeSurvey(@ApiParam(name = "requestId", value = "the ID of the survey request", required = true)
                                                   @PathParam("requestId") String requestId,
                                                   @ApiParam(name = "responseId", value = "the Qualtrics-provided response ID", required = true)
                                                   @PathParam("responseId") String responseId) {

        SurveyResponseRequest request = SurveyResponseRequest.findOne(requestId);
        request.setResponseId(responseId);
        request.setComplete(true);
        request.save();

        Patient patient = Patient.findOne(request.getPatientId());
        patient.setLastInteraction(new Date());
        patient.save();

        WSRequestHolder surveyAnswers = Qualtrics.request("getLegacyResponseData")
                .setQueryParameter("Format", "JSON")
                .setQueryParameter("SurveyID", request.getSurveyId())
                .setQueryParameter("ResponseID", responseId)
                .setQueryParameter("ExportQuestionIDs", "1");

        SurveyResponse response = new SurveyResponse();
        response.setPatientId(request.getPatientId());
        response.setSurveyId(request.getSurveyId());

        return surveyAnswers.get().map(w -> {
                    // discard any identifying data
                    ObjectNode o = (ObjectNode) w.asJson().get(responseId);
                    o.remove(Arrays.asList(new String[]{"IPAddress", "EmailAddress", "Name", "requestId", "ExternalDataReference", "ResponseSet"}));
                    response.setData(o);
                    response.save();

                    try {
                        return Rest.json(response);
                    } catch (EmptyResponseBodyException e) {
                        //TODO: replace with error
                        return Rest.error(new NoObjectsFoundError("responses"));
                    }

                }
        );

    }

    @ApiOperation(nickname = "findRequests", value = "Find survey requests", httpMethod = "GET", responseContainer = "List", response = SurveyResponseRequest.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Requests found"),
            @ApiResponse(code = 404, message = "No requests found"),
    })
    @Produces("application/json")
    public static Result findRequests(Option<String> patientId, Option<Boolean> complete, Option<Integer> limit) {

        List<SurveyResponseRequest> list;
        if (patientId.getOrElse(null) != null) {
           list = SurveyResponseRequest.findByPatientId(patientId.get(), complete.getOrElse(null), limit.getOrElse(null));
        } else {
           list = SurveyResponseRequest.find(complete.getOrElse(null), limit.getOrElse(null));
        }

        try {
            return Rest.json(list);
        } catch (EmptyResponseBodyException e) {
            return Rest.error(new NoObjectsFoundError("requests"));
        }

    }

    @ApiOperation(nickname = "findRequest", value = "Find a single survey request", httpMethod = "GET", response = SurveyResponseRequest.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Request found"),
            @ApiResponse(code = 404, message = "Request not found")
    })
    @Produces("application/json")
    public static Result findRequest(String requestId) {

        try {
            return Rest.json(SurveyResponseRequest.findOne(requestId));
        } catch (EmptyResponseBodyException e) {
            return Rest.error(new NoObjectsFoundError("request"));
        }

    }

    @ApiOperation(nickname = "findByPatientId", value = "Retrieve all responses for a single user and survey", httpMethod = "GET", response = PatientSurveyHistoryDTO.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Surveys found")})
    public static Promise<Result> findByPatientId(@ApiParam(name = "surveyId", value = "the ID of the survey", required = true)
                                                    @PathParam("surveyId") String surveyId,
                                                    @ApiParam(name = "patientId", value = "the ID of the patient", required = true)
                                                    @PathParam("patientId") String patientId) {

        WSRequestHolder surveyDef = Qualtrics.request("getSurvey").setQueryParameter("SurveyID", surveyId);

        List<SurveyResponse> responses = SurveyResponse.findByPatientId(surveyId, patientId);

        return surveyDef.get().map(response -> {

                    Document d = XML.fromString(response.getBody());

                    PatientSurveyHistoryDTO psh = new PatientSurveyHistoryDTO();

                    for (SurveyResponse r : responses) {

                        String testDate = r.getData().findValuesAsText("EndDate").get(0);
                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = formatter.parse(testDate);

                        SurveyDataDTO dataDTO = new SurveyDataDTO(date);

                        HashMap<String, String> q = new HashMap<>();

                        Iterator<Map.Entry<String, JsonNode>> it = r.getData().get(r.getData().fieldNames().next()).fields();
                        int i = 0;
                        while (it.hasNext()) {

                            Map.Entry<String, JsonNode> o = it.next();
                            if (o.getKey().matches("[Q][I][D]\\d") || o.getKey().matches("[Q][I][D]\\d[_]\\d")) {

                                dataDTO.addData(String.valueOf(i), o.getValue().asInt());

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

                    return Rest.json(psh);
                }
        );

    }

}
