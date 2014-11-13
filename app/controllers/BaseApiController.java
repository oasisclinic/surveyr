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

    public static Result options(Object wholepath) {
        return JsonResponse(null);
    }

    public static Result JsonResponse(Object obj) {
        return JsonResponse(200, obj);
    }

    public static Result JsonResponse(int code, Object obj) {

        response().setContentType("application/json");
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        response().setHeader("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization");

        return status(code, (obj == null)? null : Json.toJson(obj));
    }

}
