package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.Logger;
import play.libs.Json;
import play.libs.XML;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.util.*;

import static play.libs.F.Function;
import static play.libs.F.Promise;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result takeDemoSurvey() {

        OutstandingSurveyResult s = new OutstandingSurveyResult();
        s.setMedicalId("720326365");
        s.setSurveyId("SV_9Nd6BW5oHtdtGLP");
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

        WSRequestHolder survey = QualtricsAPI.request("getLegacyResponseData")
                .setQueryParameter("Format", "JSON")
                .setQueryParameter("SurveyID", s.getSurveyId())
                .setQueryParameter("ResponseID", responseSetId)
                .setQueryParameter("ExportQuestionIDs", "1");

        SurveyResponse x = new SurveyResponse();
        x.patientId = s.getMedicalId();
        x.surveyId = s.getSurveyId();

        final Promise<Result> resultPromise = survey.get().map(
                new Function<WSResponse, Result>() {
                    public Result apply(WSResponse response) {
                        x.data = response.asJson();
                        x.insert();
                        return ok(Json.toJson(x));
                    }
                }
        );

        return resultPromise;

    }

    public static Promise<Result> ram() {

        WSRequestHolder survey = QualtricsAPI.request("getLegacyResponseData")
                .setQueryParameter("Format", "JSON")
                .setQueryParameter("SurveyID", "SV_0JSC2ShChssXa61")
                .setQueryParameter("ResponseID", "R_1M71Wr08ZliBUMu")
                .setQueryParameter("ExportQuestionIDs", "1");

        WSRequestHolder surveyDef = QualtricsAPI.request("getSurvey")
                .setQueryParameter("SurveyID", "SV_0JSC2ShChssXa61");

        Promise<Result> results = Promise.sequence(survey.get(), surveyDef.get()).map(
                list -> {

                    JsonNode n = list.get(0).asJson().get("R_1M71Wr08ZliBUMu");
                    Document d = XML.fromString(list.get(1).getBody());

                    PatientSurveyHistoryDTO psh = new PatientSurveyHistoryDTO("R_1M71Wr08ZliBUMu", "720326365");
                    SurveyDataDTO dataDTO = new SurveyDataDTO(new Date());

                    HashMap<String, String> q = new HashMap<>();
                    Iterator<Map.Entry<String, JsonNode>> it = n.fields();
                    int i = 0;
                    while(it.hasNext()) {

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


                    psh.addData(dataDTO);
                    psh.setDefinition(q);
                    return ok(Json.toJson(psh));
                }
        );

        return results;


    }

}
