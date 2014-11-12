FROM dockerfile/ubuntu
FROM dockerfile/java:oracle-java8
MAINTAINER Bradley Davis <bradley.davis@unc.edu>

ENV ACTIVATOR_VERSION 1.2.10
ENV PATH $PATH:/tmp/activator-$ACTIVATOR_VERSION

WORKDIR /tmp

#ADD http://downloads.typesafe.com/typesafe-activator/$ACTIVATOR_VERSION/typesafe-activator-$ACTIVATOR_VERSION.zip /tmp/activator.zip
ADD /activator.zip /tmp/activator.zip
RUN unzip /tmp/activator.zip

ADD https://github.com/oasisclinic/database/archive/master.zip /tmp/master.zip
RUN unzip /tmp/master.zip

EXPOSE 9000

WORKDIR /tmp/database-master

RUN ["activator", "clean", "stage"]

RUN mkdir /app
RUN mv /tmp/database-master/target/universal/stage/ /app/

CMD ["/app/stage/bin/play-java", "-Dapplication.secret=or7_xe;JHTm4@OS`cjh/PM4=7okeqi8h^Bba0_;NiPJSvijKH^:Q>03Qygq^`W9V"]