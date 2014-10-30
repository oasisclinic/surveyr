package controllers;

import play.libs.Json;
import play.mvc.*;
import com.wordnik.swagger.core.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.StringWriter;

public class BaseApiController extends Controller {

    protected static ObjectMapper mapper = JsonUtil.mapper();

    public static Result options(Object wholepath) {
        return JsonResponse(null);
    }

    public static Result JsonResponse(Object obj) {
        return JsonResponse(obj, 200);
    }

    public static Result JsonResponse(Object obj, int code) {

        response().setContentType("application/json");
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        response().setHeader("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization");

        return ok(Json.toJson(obj));
    }

}
