FROM amazoncorretto:11
VOLUME /tmp
COPY ./build/libs/ms-orders-kafka-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir uatappinsight 
COPY ./applicationinsights-agent-3.2.1.jar uatappinsight
COPY ./applicationinsights.json uatappinsight
EXPOSE 8201
#ENTRYPOINT ["java","-jar","/app.jar"]
ENTRYPOINT ["java","-javaagent:uatappinsight/applicationinsights-agent-3.2.1.jar","-jar","/app.jar"]