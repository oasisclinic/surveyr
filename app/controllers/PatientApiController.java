package controllers;

import com.wordnik.swagger.annotations.*;
import models.Patient;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.UUID;

@Api(value = "/api/patients", description = "Operations involving patients")
public class PatientApiController extends BaseApiController {

    @ApiOperation(nickname="create", value = "Create a patient record", httpMethod = "POST", response = Patient.class)
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
            patient.save();
            return JsonResponse(201, patient);
        }

    }

    @ApiOperation(nickname="findAll", value = "Get all patient records", httpMethod = "GET", responseContainer = "List", response = Patient.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Patient records found"),
            @ApiResponse(code = 404, message = "No patient records found")
    })
    @Produces("application/json")
    public static Result findAll() {

        List<Patient> patients = Patient.findAll();

        if (patients.size() > 0) {
            return JsonResponse(200, patients);
        } else {
            return JsonResponse(404, patients);
        }

    }

}