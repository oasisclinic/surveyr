package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.core.util.JsonUtil;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class BaseApiController extends Controller {

    protected static ObjectMapper mapper = JsonUtil.mapper();

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

        return status(code, (obj == null) ? null : Json.toJson(obj));
    }

}
