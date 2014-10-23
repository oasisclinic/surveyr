package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.OutstandingSurveyResult;
import models.QualtricsAPI;
import models.SurveyResponse;
import org.w3c.dom.Document;
import play.libs.Json;
import play.libs.XML;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.util.UUID;

import static play.libs.F.Function;
import static play.libs.F.Promise;

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
        url += "&ResponseID=" + responseSetId;

        SurveyResponse x = new SurveyResponse();
        x.patientId = s.getMedicalId();
        x.surveyId = s.getSurveyId();

        final Promise<Result> resultPromise = WS.url(url).get().map(
                new Function<WSResponse, Result>() {
                    public Result apply(WSResponse response) {
                        x.data = response.asJson();
                        x.insert();
                        return ok(Json.toJson(s));
                    }
                }
        );

        return resultPromise;

    }

    public static Promise<Result> ram() {

        WSRequestHolder survey = QualtricsAPI.request("getLegacyResponseData")
                .setQueryParameter("Format", "JSON")
                .setQueryParameter("SurveyID", "SV_0JSC2ShChssXa61")
                .setQueryParameter("ResponseID", "R_b18rd1LAM7muty5")
                .setQueryParameter("ExportQuestionIDs", "1");

        WSRequestHolder surveyDef = QualtricsAPI.request("getSurvey")
                .setQueryParameter("SurveyID", "SV_0JSC2ShChssXa61");

        Promise<Result> results = Promise.sequence(survey.get(), surveyDef.get()).map(
                list -> {
                    JsonNode n = list.get(0).asJson();
                    Document d = XML.fromString(list.get(1).getBody());
                    return ok(Json.toJson(n));
                }
        );

        return results;


    }

}
