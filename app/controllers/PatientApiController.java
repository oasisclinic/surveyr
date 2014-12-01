package controllers;

import com.wordnik.swagger.annotations.*;
import errors.EmptyResponseBodyException;
import errors.NoPatientsFoundError;
import models.Patient;
import play.Configuration;
import play.Play;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;
import utilities.RestResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Api(value = "/api/patients", description = "Operations involving patients")
public class PatientApiController extends BaseApiController {

    private static final Configuration conf = Play.application().configuration();
    private static final String NO_PATIENT_FOUND = conf.getString("errors.nosuchpatient");
    private static final String INVALID_PARAMETER = conf.getString("errors.invalidparameter");

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

    @ApiOperation(nickname = "findAll", value = "Finds all patients", httpMethod = "GET", responseContainer = "List", response = Patient.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Patients found"),
            @ApiResponse(code = 400, message = "Limit parameter must be >= -1"),
            @ApiResponse(code = 404, message = "No patients found")
    })
    @Produces("application/json")
    public static Result findAll(@ApiParam(name = "limit", value = "the number of patients to return", required = false)
                                 @PathParam("limit") Integer limit) {
        if (limit < -1) {
            return JsonResponse(400, INVALID_PARAMETER);
        }

        List<Patient> patients = null;
        if (limit == -1) {
            patients = Patient.findAll();
        } else {
            patients = Patient.findMostRecent(limit);
        }

        if (patients == null) {
            return JsonResponse(404, NO_PATIENT_FOUND);
        } else {
            return JsonResponse(200, patients);
        }

    }

    @ApiOperation(nickname = "findById", value = "Finds a patient by his identifier", httpMethod = "GET", response = Patient.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Patient found"),
            @ApiResponse(code = 404, message = "No such patient found")
    })
    @Produces("application/json")
    public static Result findById(@ApiParam(name = "patientId", value = "patientId", required = true)
                                  @PathParam("patientId") String patientId) {

        try {
            return RestResponse.json(200, Patient.findOne(patientId));
        } catch (EmptyResponseBodyException e) {
            return RestResponse.error(new NoPatientsFoundError());
        }

    }

}