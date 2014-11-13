# OASIS Surveyor
collects and aggregates Qualtrics surveys

Running Surveyor on Ubuntu with Docker and DockerHub
---------------
Install Docker by following these [instructions](https://docs.docker.com/installation/ubuntulinux/)

Then, run
```
sudo docker run -d -p 27017:27017 --name mongodb dockerfile/mongodb 
sudo docker run -d -p 80:9000 --link mongodb:mongo oasisclinic/surveyor
```
These commands create a Docker container running MongoDB on port 27017 and links it to a container which has the latest Play framework code from master