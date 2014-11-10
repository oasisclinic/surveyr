package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.*;
import com.wordnik.swagger.core.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.StringWriter;

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
