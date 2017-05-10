# Extend tomcat7 container with jre8
FROM tomcat:7-jre8
MAINTAINER "Christos Petsos <chrispetsos@gmail.com">

# Remove all artifacts existing in ROOT folder
RUN ["rm", "-rf", "/usr/local/tomcat/webapps/ROOT"]
# Copy contents of exploded .war application inside the container at ROOT folder.
# Note that the actual packaged .war file is omitted with the .dockerignore file.
COPY ./ic-query-validator-rest/target/ic-query-validator-rest-* /usr/local/tomcat/webapps/ROOT/

# Expose Tomcat port from within the container.
EXPOSE 8080
# Start Tomcat
CMD ["catalina.sh", "run"]
