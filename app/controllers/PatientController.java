package controllers;

import com.wordnik.swagger.annotations.*;
import errors.EmptyResponseBodyException;
import errors.InvalidParameterError;
import errors.NoObjectsFoundError;
import models.Patient;
import play.data.Form;
import play.mvc.*;
import play.mvc.Security;
import utilities.Rest;
import utilities.Secure;

import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Security.Authenticated(Secure.class)
@Api(value = "/api/patients", description = "Operations involving patients")
public class PatientController extends Controller {

    @ApiOperation(nickname = "create", value = "Create a patient record", httpMethod = "POST", response = Patient.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "New patient successfully created")})
    @ApiImplicitParams(value = {@ApiImplicitParam(value = "Patient object to create", name = "body", required = true, paramType = "body", dataType = "Patient")})
    @Consumes("application/json")
    @Produces("application/json")
    @BodyParser.Of(BodyParser.Json.class)
    public static Result create() {

        Form<Patient> form = Form.form(Patient.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest();
        } else {
            Patient patient = form.get();
            patient.setPatientId(UUID.randomUUID());
            patient.setLastInteraction(new Date());
            patient.save();
            try {
                return Rest.json(201, patient);
            } catch (EmptyResponseBodyException e) {
                return internalServerError();
            }
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
            return Rest.error(new InvalidParameterError());
        }

        List<Patient> patients = null;
        if (limit == -1) {
            patients = Patient.findAll();
        } else {
            patients = Patient.findMostRecent(limit);
        }

        try {
            return Rest.json(patients);
        } catch (EmptyResponseBodyException e) {
            return Rest.error(new NoObjectsFoundError("patients"));
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
            return Rest.json(Patient.findOne(patientId));
        } catch (EmptyResponseBodyException e) {
            return Rest.error(new NoObjectsFoundError("patient"));
        }

    }

}