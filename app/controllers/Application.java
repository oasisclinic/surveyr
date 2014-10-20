package controllers;

import models.OutstandingSurveyResult;

import play.libs.ws.*;
import play.mvc.Result;

import static play.libs.F.Function;
import static play.libs.F.Promise;
import play.mvc.*;

import views.html.*;

import java.util.UUID;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result takeDemoSurvey() {

        OutstandingSurveyResult s = new OutstandingSurveyResult();
        s.setMedicalId("720326365");
        s.setSurveyId("SV_0JSC2ShChssXa61");
        s.setRequestId(UUID.randomUUID().toString());
        s.insert();

        String url = "https://unc.az1.qualtrics.com/SE/?";
        url += "SID=" + s.getSurveyId();
        url += "&requestId=" + s.getRequestId();

        return redirect(url);
    }

    public static Promise<Result> completeDemoSurvey(String requestId, String responseSetId) {

        OutstandingSurveyResult s = OutstandingSurveyResult.findByRequestId(requestId);
        s.setResponseSetId(responseSetId);

        String url = "https://survey.qualtrics.com/WRAPI/ControlPanel/api.php?Request=getLegacyResponseData&Format=JSON&User=720326365%23unc&Token=czqtiOXIJOk7nbZ0J8wlfiYn2wNjhKxntyhRChXi&Version=2.4";
        url += "&SurveyID=" + s.getSurveyId();
        url += "&ResponseID=" + s.getResponseSetId();

        final Promise<Result> resultPromise = WS.url(url).get().map(
                new Function<WSResponse, Result>() {
                    public Result apply(WSResponse response) {
                        return ok("Response:" + response.asJson());
                    }
                }
        );

        return resultPromise;

    }

}
