package controllers;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import models.Evaluation;
import play.libs.F.Promise;
import play.libs.ws.WSRequestHolder;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Qualtrics;
import utilities.Rest;

import javax.ws.rs.PathParam;

public class EvaluationController extends Controller {

    @ApiOperation(nickname = "requestStart", value = "Request an evaluation", httpMethod = "GET", response = Evaluation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Evaluation request created")
    })
    public static Promise<Result> requestStart(@ApiParam(name = "surveyId", value = "the Qualtrics survey ID to administer", required = true)
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
        return redirect(Qualtrics.createSurveyUrl(eval.getSurveyId(), eval.getEvaluationId()));

    }

}
