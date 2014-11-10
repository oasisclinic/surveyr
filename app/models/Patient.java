package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import play.data.validation.Constraints.*;
import uk.co.panaxiom.playjongo.PlayJongo;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@ApiModel(value = "A patient is a client of the clinic")
public class Patient {

    @JsonIgnore
    private static final MongoCollection collection = PlayJongo.getCollection("patients");

    @JsonProperty("_id")
    private ObjectId _id;

    @Required(message = "You must provide the medical ID of the patient")
    @ApiModelProperty(value = "the UNC medical ID of the patient", required = true)
    private String medicalId;

    @Required(message = "You must provide the first name of the patient")
    @ApiModelProperty(value = "the legal given name of the patient", required = true)
    private String firstName;

    @Required(message = "You must provide the last name of the patient")
    @ApiModelProperty(value = "the legal surname of the patient", required = true)
    private String lastName;

    @ApiModelProperty(value = "the date of the patient's last visit to the clinic", required = false)
    private Date lastVisit;

    public Patient save() {
        collection.save(this);
        return this;
    }

    public static List<Patient> findAll() {
        List<Patient> patients = new LinkedList<>();
        Iterator<Patient> it = collection.find().as(Patient.class).iterator();
        while(it.hasNext()) patients.add(it.next());
        return patients;
    }

    public static Patient findOne(String id) {
        return collection.findOne("{_id: #}", id).as(Patient.class);
    }

    public String getMedicalId() {
        return medicalId;
    }

    public void setMedicalId(String medicalId) {
        this.medicalId = medicalId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public static MongoCollection getCollection() {
        return collection;
    }

    public Date getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(Date lastVisit) {
        this.lastVisit = lastVisit;
    }

}