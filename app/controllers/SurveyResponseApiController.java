package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordnik.swagger.annotations.*;
import models.PatientSurveyHistoryDTO;
import models.QualtricsAPI;
import models.SurveyDataDTO;
import models.SurveyResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.XML;
import play.libs.ws.WSRequestHolder;
import play.mvc.*;

import javax.ws.rs.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Api(value = "/api/surveyResponses", description = "Operations involving survey data retrieval.")
public class SurveyResponseApiController extends BaseApiController {

    @ApiOperation(nickname="findByPatientId", value = "Add a new pet to the store", httpMethod = "GET", responseContainer = "List", response = SurveyResponse.class)
    @ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
    public static F.Promise<Result> findByPatientId(@PathParam("surveyId") String surveyId, @PathParam("patientId") String patientId) {

        WSRequestHolder surveyDef = QualtricsAPI.request("getSurvey")
                .setQueryParameter("SurveyID", surveyId);

        List<SurveyResponse> responses = SurveyResponse.findByPatientId(surveyId, patientId);


        F.Promise<Result> results = F.Promise.sequence(surveyDef.get()).map(
                list -> {

                    Document d = XML.fromString(list.get(0).getBody());

                    PatientSurveyHistoryDTO psh = new PatientSurveyHistoryDTO(surveyId, patientId);

                    Logger.debug("responses found: " + responses.size());

                    for (SurveyResponse r : responses) {

                        String testDate = r.data.findValuesAsText("EndDate").get(0);
                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = formatter.parse(testDate);

                        SurveyDataDTO dataDTO = new SurveyDataDTO(date);

                        HashMap<String, String> q = new HashMap<>();

                        Logger.info(Json.toJson(r.data).toString());

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
