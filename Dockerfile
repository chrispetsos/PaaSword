FROM tomcat:7-jre8
MAINTAINER "Christos Petsos <chrispetsos@gmail.com">

RUN ["rm", "-rf", "/usr/local/tomcat/webapps/ROOT"]
COPY ./ic-query-validator-rest/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
