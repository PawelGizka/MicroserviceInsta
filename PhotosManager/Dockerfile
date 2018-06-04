FROM openjdk:8

RUN mkdir -p /opt/app
WORKDIR /opt/app

EXPOSE 8080

COPY ./run_jar.sh ./target/scala-2.12/photos-assembly.jar ./

#ENTRYPOINT ["./run_jar.sh"]
CMD java -jar photos-assembly.jar