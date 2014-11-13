package controllers;

import com.wordnik.swagger.annotations.*;
import models.Patient;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;

import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Api(value = "/api/patients", description = "Operations involving patients")
public class PatientApiController extends BaseApiController {

    @ApiOperation(nickname = "create", value = "Create a patient record", httpMethod = "POST", response = Patient.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "New patient successfully created")})
    @ApiImplicitParams(value = {@ApiImplicitParam(value = "Patient object to create", name = "body", required = true, paramType = "body", dataType = "Patient")})
    @Consumes("application/json")
    @Produces("application/json")
    @BodyParser.Of(BodyParser.Json.class)
    public static Result create() {

        Form<Patient> form = Form.form(Patient.class).bindFromRequest();
        if (form.hasErrors()) {
            return JsonResponse(400, form.errorsAsJson());
        } else {
            Patient patient = form.get();
            patient.setPatientId(UUID.randomUUID());
            patient.setLastInteraction(new Date());
            patient.save();
            return JsonResponse(201, patient);
        }

    }

    @ApiOperation(nickname = "findAll", value = "Get patient records", httpMethod = "GET", responseContainer = "List", response = Patient.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Patient records found")
    })
    @Produces("application/json")
    public static Result findAll(@ApiParam(name = "limit", value = "the number of patients to return", required = false)
                                 @PathParam("limit") Integer limit) {
        if (limit < -1) {
            return JsonResponse(400, "limit must be greater than or equal to -1");
        } else if (limit == -1) {
            return JsonResponse(Patient.findAll());
        } else {
            return JsonResponse(Patient.findMostRecent(limit));
        }

    }

    @ApiOperation(nickname = "findById", value = "Get patient information", httpMethod = "GET", response = Patient.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Patient found")
    })
    @Produces("application/json")
    public static Result findById(@ApiParam(name = "patientId", value = "patientId", required = true)
                                  @PathParam("patientId") String patientId) {
        Patient patient = Patient.findOne(patientId);
        return JsonResponse(patient == null ? 404 : 200, patient);

    }

}