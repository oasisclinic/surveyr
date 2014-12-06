package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordnik.swagger.annotations.*;
import errors.EmptyResponseBodyException;
import errors.InvalidParameterError;
import errors.NoObjectsFoundError;
import models.Evaluation;
import models.Patient;
import models.dto.UrlContainer;
import play.Logger;
import play.libs.F;
import play.libs.ws.WSRequestHolder;
import play.mvc.Result;
import utilities.Qualtrics;
import utilities.Rest;

import javax.ws.rs.PathParam;
import java.util.Arrays;
import java.util.Date;

/**
 * Handles taking surveys and completing them (unauthenticated).
 * @author Bradley Davis
 */
@Api(value = "/api/evaluations", description = "Handles public evaluation operations")
public class PublicController {

    /**
     * Returns a Qualtrics url to take a survey
     * @param pin the pin code of the survey
     * @return URL of Qualtrics survey
     */
    @ApiOperation(nickname = "start", value = "Take an evaluation using a pin code", httpMethod = "GET", response=UrlContainer.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "URL of survey returned"),
            @ApiResponse(code = 400, message = "Bad pin code")
    })
    public static Result start(@ApiParam(name = "pin", value = "the pin code for the evaluation", required = true)
                               @PathParam("pin") Integer pin) {

        Evaluation eval = Evaluation.findOneByField("pinCode", pin);

        try {
            if(eval == null) {
                return Rest.error(new InvalidParameterError());
            }
            return Rest.json(new UrlContainer(Qualtrics.createSurveyUrl(eval.getSurveyId(), eval.getEvaluationId())));
        } catch (EmptyResponseBodyException e) {
            return Rest.error(new InvalidParameterError());
        }

    }

    /**
     * Completes a survey by getting result data from Qualtrics and updating evaluation records
     * @param requestId the ID of the survey request
     * @param responseId the Qualtrics-provided response ID
     * @return the evaluation that was completed
     */
    @ApiOperation(nickname = "complete", value = "Complete an evaluation", httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Survey response recorded")
    })
    public static F.Promise<Result> complete(@ApiParam(name = "requestId", value = "the ID of the survey request", required = true)
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
}
