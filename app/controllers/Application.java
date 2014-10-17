package controllers;

import models.User;
import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result redirect() {
        return redirect("http://www.google.com");
    }

    public static Result add() {

        User u = new User();
        u.name = "Bradley";
        u.insert();
        return ok("Inserted");

    }

    public static Result show() {

        User u = User.findByName("Bradley");
        return ok(u.name);

    }

}
