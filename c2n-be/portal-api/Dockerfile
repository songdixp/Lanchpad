FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/openjdk:8-alpine-linuxarm64
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} app.jar
ENV TZ=Asia/Shanghai
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
