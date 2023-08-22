FROM adoptopenjdk/openjdk11:alpine-jre 
RUN apk --update add fontconfig ttf-dejavu
ARG JAR_FILE=target/thirdproj-0.0.1-SNAPSHOT.jar  
WORKDIR /opt/app  
COPY ${JAR_FILE} app.jar  
ENTRYPOINT ["java","-jar","app.jar"] 

