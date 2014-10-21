package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import play.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SurveyResponse {

    @JsonProperty("_id")
    public ObjectId id;

    public String prop;

    public Set<JsonNode> answers;

    public SurveyResponse() {

    }

//    public static SurveyResponse createFromResult(JsonNode source) {
//
//        Logger.debug("entering");
//        SurveyResponse s = new SurveyResponse();
//        ObjectMapper mapper = new ObjectMapper();
//        HashSet<JsonNode> props = new HashSet<>();
//
//        Iterator it = source.fields();
//        while(it.hasNext());
//        {
//            Map.Entry<String, JsonNode> elt = (Map.Entry<String, JsonNode>) it.next();
//            Logger.debug("checking " + elt.getKey());
//            if (elt.getKey().contains("QID"))
//            {
//                Logger.debug("added value");
//                props.add(elt.getValue());
//            }
//        }
//
//        s.answers = props;
//        return s;
//
//    }



}
