FROM openjdk:11
VOLUME /tmp
EXPOSE 8014
ADD ./target/currentaccount-0.0.1-SNAPSHOT.jar currentaccount.jar
ENTRYPOINT ["java","-jar","/savingaccount.jar"]