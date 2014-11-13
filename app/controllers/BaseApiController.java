package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.wordnik.swagger.core.util.JsonUtil;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class BaseApiController extends Controller {

    private static SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept("_id");
    private static FilterProvider provider = new SimpleFilterProvider().addFilter("no id", filter);
    //private static ObjectMapper mapper = JsonUtil.mapper().copy();

    public BaseApiController() {
        //mapper.setFilters(provider);
    }

    public static Result options(Object wholepath) {

        response().setContentType("application/json");
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        response().setHeader("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization");
        return ok();

    }

    public static Result JsonResponse(Object obj) {
        return JsonResponse(200, obj);
    }

    public static Result JsonResponse(int code, Object obj) {

        response().setContentType("application/json");
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        response().setHeader("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization");

        ObjectMapper m = new ObjectMapper();
        //JsonNode n = (obj == null)? null : m.valueToTree(obj);
        String n = null;
        try {
            n = m.writer(new SimpleFilterProvider().addFilter("asdf", SimpleBeanPropertyFilter.serializeAllExcept("firstName"))).writeValueAsString(obj);
            Logger.info(n);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return status(code, n);
    }

}
