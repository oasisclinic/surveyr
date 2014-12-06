package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.wordnik.swagger.annotations.*;
import errors.EmptyResponseBodyException;
import errors.NoObjectsFoundError;
import models.Evaluation;
import models.dto.PatientSurveyHistoryDTO;
import models.dto.SurveyDataDTO;
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

/**
 * Handles all operations on evaluations.
 * @author Bradley Davis
 */
@Api(value = "/api/evaluations", description = "Operations involving evaluations")
@Security.Authenticated(Secure.class)
public class EvaluationController extends Controller {

    /**
     * Generates a pin code to create a survey
     * @param surveyId the survey ID to take
     * @param patientId the patient ID to associate with a survey
     * @return an evaluation containing the pin code
     */
    @ApiOperation(nickname = "request", value = "Request an evaluation", httpMethod = "GET", response = Evaluation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Evaluation request created"),
            @ApiResponse(code = 401, message = "Unauthorized")
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

    /**
     * Returns recent evaluations
     * @param limit the number of evaluations to return
     * @return recent evaluations
     */
    @ApiOperation(nickname = "recent", value = "Get recent evaluations", httpMethod = "GET", response = Evaluation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Evaluations found"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "No evaluations found")
    })
    public static Result recent(@ApiParam(name = "limit", value = "the number of evaluations to return", required = false)
                                @PathParam("limit") Integer limit) {

        List<Evaluation> list = Evaluation.findRecent(limit);

        try {
            return Rest.json(list);
        } catch (EmptyResponseBodyException e) {
            return Rest.error(new NoObjectsFoundError("evaluations"));
        }

    }

    /**
     * Gets evaluations by patient ID
     * @param patientId the patient ID
     * @return evaluations
     */
    @ApiOperation(nickname = "findByPatientId", value = "Get evaluations by patientId", httpMethod = "GET", response = Evaluation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Evaluations found"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "No evaluations found")
    })
    public static Result findAllByPatientId(@ApiParam(name = "patientId", value = "the ID of the patient", required = true)
                                            @PathParam("patientId") String patientId) {

        List<Evaluation> list = Evaluation.findByField("patientId", patientId);

        try {
            return Rest.json(list);
        } catch (EmptyResponseBodyException e) {
            return Rest.error(new NoObjectsFoundError("evaluations"));
        }
    }

    /**
     * Gets an evaluation by ID
     * @param evaluationId the evaluation ID
     * @return an evaluation
     */
    @ApiOperation(nickname = "findById", value = "Get evaluation by ID", httpMethod = "GET", response = Evaluation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Evaluation found"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "No evaluation found")
    })
    public static Result findById(@ApiParam(name = "evaluationId", value = "the ID of the evaluation", required = true)
                                            @PathParam("evaluationId") String evaluationId) {

        Evaluation eval = Evaluation.findOneByField("evaluationId", evaluationId);

        try {
            return Rest.json(eval);
        } catch (EmptyResponseBodyException e) {
            return Rest.error(new NoObjectsFoundError("evaluation"));
        }
    }

    /**
     * Deletes an evaluation
     * @param evaluationId the evaluation ID
     * @return an evaluation that has been deleted
     */
    @ApiOperation(nickname = "deleteById", value = "Delete evaluation", httpMethod = "DELETE", response = Evaluation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Evaluation deleted"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "No evaluations found")
    })
    public static Result deleteById(@ApiParam(name = "evaluationId", value = "the ID of the evaluation", required = true)
                                  @PathParam("evaluationId") String evaluationId) {

        Evaluation eval = Evaluation.findOneByField("evaluationId", evaluationId);

        try {
            return Rest.json(eval.remove());
        } catch (EmptyResponseBodyException e) {
            return Rest.error(new NoObjectsFoundError("evaluation"));
        }

    }

    /**
     * Finds evaluations for one patient and one survey
     * @param patientId the patient ID
     * @param surveyId the survey ID
     * @return evaluations
     */
    @ApiOperation(nickname = "findByPatientId", value = "Retrieve all responses for a single user and survey", httpMethod = "GET", response = PatientSurveyHistoryDTO.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Surveys found")})
    public static Promise<Result> findByPatientIdSurveyId(@ApiParam(name = "patientId", value = "the ID of the patient", required = true)
                                                  @PathParam("patientId") String patientId,
                                                  @ApiParam(name = "surveyId", value = "the ID of the survey", required = true)
                                                  @PathParam("surveyId") String surveyId) {

        // Get survey definitions
        WSRequestHolder surveyDef = Qualtrics.request("getSurvey").setQueryParameter("SurveyID", surveyId);

        // Get evaluations
        List<Evaluation> responses = Evaluation.findByPatientIdSurveyId(patientId, surveyId);

        // Then
        return surveyDef.get().map(response -> {

                    // Get XML from survey definition (no JSON available)
                    Document d = XML.fromString(response.getBody());

                    // Create a DTO
                    PatientSurveyHistoryDTO psh = new PatientSurveyHistoryDTO();

                    for (Evaluation eval : responses) {

                        // Extract end date of the survey
                        String testDate = eval.getData().findValuesAsText("EndDate").get(0);
                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = formatter.parse(testDate);

                        // Create single response DTO
                        SurveyDataDTO dataDTO = new SurveyDataDTO(date);

                        HashMap<String, String> q = new HashMap<>();

                        // Get data fields
                        Iterator<Map.Entry<String, JsonNode>> it = eval.getData().fields();
                        int i = 0;

                        // For all fields, if key is either QID_nubmber or QIDnumber_number
                        while (it.hasNext()) {

                            Map.Entry<String, JsonNode> o = it.next();
                            if (o.getKey().matches("[Q][I][D]\\d") || o.getKey().matches("[Q][I][D]\\d[_]\\d")) {

                                // Add question answer
                                dataDTO.addData(String.valueOf(i), o.getValue().asInt());

                                // Get question text
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

                        // add definition and data to response
                        psh.setDefinition(q);
                        psh.addData(dataDTO);

                    }

                    // return DTO with graph data
                    return Rest.json(psh);
                }
        );

    }

}
