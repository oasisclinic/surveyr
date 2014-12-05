package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import errors.EmptyResponseBodyException;
import errors.InvalidParameterError;
import errors.NoObjectsFoundError;
import models.Evaluation;
import models.Patient;
import models.SurveyResponse;
import models.SurveyResponseRequest;
import models.dto.PatientSurveyHistoryDTO;
import models.dto.SurveyDataDTO;
import models.dto.UrlDto;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.Logger;
import play.libs.F.Promise;
import play.libs.XML;
import play.libs.ws.WSRequestHolder;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Qualtrics;
import utilities.Rest;
import utilities.Secure;

import javax.ws.rs.PathParam;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

//@Security.Authenticated(Secure.class)
public class EvaluationController extends Controller {

    @ApiOperation(nickname = "request", value = "Request an evaluation", httpMethod = "GET", response = Evaluation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Evaluation request created")
    })
    public static Promise<Result> request(@ApiParam(name = "surveyId", value = "the Qualtrics survey ID to administer", required = true)
                                               @PathParam("surveyId") String surveyId,
                                               @ApiParam(name = "patientId", value = "the ID of the patient to administer a survey to", required = true)
                                               @PathParam("patientId") String patientId) {

        WSRequestHolder getSurveyName = Qualtrics.request("getSurveyName")
                .setQueryParameter("Format", "JSON")
                .setQueryParameter("SurveyID", surveyId);

        return getSurveyName.get().map(response -> {

                    String surveyName = response.asJson().with("Result").get("SurveyName").asText();
                    Evaluation evaluation = new Evaluation(patientId, surveyId, surveyName).save();
                    return Rest.json(evaluation);

                }
        );

    }

    @ApiOperation(nickname = "start", value = "Take an evaluation", httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 302, message = "Evaluation in progress")
    })
    public static Result start(@ApiParam(name = "pin", value = "the pin code for the evaluation", required = true)
                                        @PathParam("pin") Integer pin) {

        Evaluation eval = Evaluation.findOneByField("pinCode", pin);

        try {
            if(eval == null) {
                return Rest.error(new InvalidParameterError());
            }
            return Rest.json(new UrlDto(Qualtrics.createSurveyUrl(eval.getSurveyId(), eval.getEvaluationId())));
        } catch (EmptyResponseBodyException e) {
            return Rest.error(new InvalidParameterError());
        }

    }

    @ApiOperation(nickname = "complete", value = "Complete an evaluation", httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Survey response recorded")
    })
    public static Promise<Result> complete(@ApiParam(name = "requestId", value = "the ID of the survey request", required = true)
                                                 @PathParam("requestId") String requestId,
                                                 @ApiParam(name = "responseId", value = "the Qualtrics-provided response ID", required = true)
                                                 @PathParam("responseId") String responseId) {

        Evaluation eval = Evaluation.findOneByField("evaluationId", requestId);
        eval.setResponseId(responseId);
        eval.setEndDate(new Date());
        eval.setComplete(true);

        Logger.debug(eval.toString());

        Patient patient = Patient.findOne(eval.getPatientId());
        patient.setLastInteraction(new Date());
        patient.save();

        WSRequestHolder surveyAnswers = Qualtrics.request("getLegacyResponseData")
                .setQueryParameter("Format", "JSON")
                .setQueryParameter("SurveyID", eval.getSurveyId())
                .setQueryParameter("ResponseID", responseId)
                .setQueryParameter("ExportQuestionIDs", "1");

        return surveyAnswers.get().map(w -> {
                    // discard any identifying data
                    ObjectNode o = (ObjectNode) w.asJson().get(responseId);
                    o.remove(Arrays.asList(new String[]{"IPAddress", "EmailAddress", "Name", "requestId", "ExternalDataReference", "ResponseSet"}));
                    eval.setData(o);
                    eval.save();

                    Logger.debug(eval.toString());

                    try {
                        return Rest.json(eval);
                    } catch (EmptyResponseBodyException e) {
                        //TODO: replace with error
                        return Rest.error(new NoObjectsFoundError("responses"));
                    }

                }
        );

    }

    public static Result findAllByPatientId(@ApiParam(name = "patientId", value = "the ID of the patient", required = true)
                                            @PathParam("patientId") String patientId) {

        List<Evaluation> list = Evaluation.findByField("patientId", patientId);

        try {
            return Rest.json(list);
        } catch (EmptyResponseBodyException e) {
            return Rest.error(new NoObjectsFoundError("evaluations"));
        }
    }

    public static Result findById(@ApiParam(name = "evaluationId", value = "the ID of the evaluation", required = true)
                                            @PathParam("evaluationId") String evaluationId) {

        Evaluation eval = Evaluation.findOneByField("evaluationId", evaluationId);

        try {
            return Rest.json(eval);
        } catch (EmptyResponseBodyException e) {
            return Rest.error(new NoObjectsFoundError("evaluation"));
        }
    }

    public static Result deleteById(@ApiParam(name = "evaluationId", value = "the ID of the evaluation", required = true)
                                  @PathParam("evaluationId") String evaluationId) {

        Evaluation eval = Evaluation.findOneByField("evaluationId", evaluationId);

        try {
            return Rest.json(eval.remove());
        } catch (EmptyResponseBodyException e) {
            return Rest.error(new NoObjectsFoundError("evaluation"));
        }

    }


    @ApiOperation(nickname = "findByPatientId", value = "Retrieve all responses for a single user and survey", httpMethod = "GET", response = PatientSurveyHistoryDTO.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Surveys found")})
    public static Promise<Result> findByPatientIdSurveyId(@ApiParam(name = "patientId", value = "the ID of the patient", required = true)
                                                  @PathParam("patientId") String patientId,
                                                  @ApiParam(name = "surveyId", value = "the ID of the survey", required = true)
                                                  @PathParam("surveyId") String surveyId) {

        WSRequestHolder surveyDef = Qualtrics.request("getSurvey").setQueryParameter("SurveyID", surveyId);

        List<Evaluation> responses = Evaluation.findByPatientIdSurveyId(patientId, surveyId);
        Logger.info("found: " + responses);

        return surveyDef.get().map(response -> {

                    Document d = XML.fromString(response.getBody());

                    PatientSurveyHistoryDTO psh = new PatientSurveyHistoryDTO();

                    for (Evaluation eval : responses) {

                        String testDate = eval.getData().findValuesAsText("EndDate").get(0);
                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = formatter.parse(testDate);

                        SurveyDataDTO dataDTO = new SurveyDataDTO(date);

                        HashMap<String, String> q = new HashMap<>();

                        Iterator<Map.Entry<String, JsonNode>> it = eval.getData().fields();
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
