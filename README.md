# OASIS Surveyr
Surveyr is built for Outreach and Support Intervention Services at The University of North Carolina at Chapel Hill to collect and aggregate Qualtrics surveys.

**DISCLAIMER: This or any product associated with these accounts are not HIPPA compliant. HIPPA compliance requires continual maintenance and observation, as well as many features these applications lack such as protocols for provisioning new users. DO NOT USE THIS IN A HIPPA SETTING AS IS!**

This repository houses all backend code necessary to produce the Surveyr API as well as the latest distribution version of the frontend application. To see the source of the frontend application, please check out [Displayr](https://github.com/oasisclinic/displayr).

Stack
---------------
Surveyr is built with:
- Play Framework (Java)
- AngularJS
- MongoDB

Documentation
---------------
You can explore our API by plugging in the URLs [http://54.173.152.217/swagger/patients,  http://54.173.152.217/swagger/surveys, http://54.173.152.217/swagger/evaluations, http://54.173.152.217/swagger/security] into [Swagger UI](http://54.173.152.217/swagger/index.html). We also have a Javadoc [here](http://54.173.152.217/javadoc/index.html).

Running Surveyor on Ubuntu with Docker and DockerHub
---------------
Install Docker by following these [instructions](https://docs.docker.com/installation/ubuntulinux/).

Then, run
```
sudo docker run -d -p 27017:27017 --name mongodb dockerfile/mongodb 
sudo docker run -d -p 80:9000 --link mongodb:mongo oasisclinic/surveyr
```
The oasisclinic/surveyr image is retrieved from DockerHub which hosts an automated build repository based upon the master branch of this repository.
