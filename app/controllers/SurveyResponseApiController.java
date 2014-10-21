package controllers;

import com.wordnik.swagger.annotations.*;
import models.SurveyResponse;
import play.libs.Json;
import play.mvc.*;

import javax.ws.rs.*;

@Api(value = "/api/surveyResponses", description = "Operations involving survey data retrieval.")
public class SurveyResponseApiController extends BaseApiController {

    @ApiOperation(nickname="findById", value = "Add a new pet to the store", httpMethod = "GET", response = SurveyResponse.class)
    @ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
    public static Result findById(@PathParam("id") String id) {

        SurveyResponse s = new SurveyResponse();
        s.prop = "asdfasdf";
        return JsonResponse(s);

    }

}
