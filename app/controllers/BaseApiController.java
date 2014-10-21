package controllers;

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
        StringWriter w = new StringWriter();
        try {
            mapper.writeValue(w, obj);
        } catch (Exception e) {
            // TODO: handle proper return code
            e.printStackTrace();
        }

        response().setContentType("application/json");
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        response().setHeader("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization");

        return ok(w.toString());
    }

}
