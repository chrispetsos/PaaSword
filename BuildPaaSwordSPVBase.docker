FROM tomcat:7-jre8
MAINTAINER "Christos Petsos <chrispetsos@gmail.com">

RUN ["rm", "-rf", "/usr/local/tomcat/webapps/ROOT"]
COPY ./ic-query-validator-rest/target/ic-query-validator-rest-* /usr/local/tomcat/webapps/ROOT/

EXPOSE 8080
CMD ["catalina.sh", "run"]
