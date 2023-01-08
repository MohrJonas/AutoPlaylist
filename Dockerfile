FROM maven:3-ibm-semeru-17-focal AS builder

RUN DEBIAN_FRONTEND=noninteractive apt update
RUN DEBIAN_FRONTEND=noninteractive apt install --no-install-recommends --no-intall--suggests -y git

WORKDIR /

RUN git clone https://github.com/MohrJonas/autoPlaylist

WORKDIR /autoPlaylist

RUN mvn clean package

FROM bellsoft/liberica-openjdk-debian:17-x86_64

COPY --from=builder /autoPlaylist/target/AutoPlaylist-1.0-SNAPSHOT.jar /

ENTRYPOINT ["java", "-server", "-jar", "/AutoPlaylist-1.0-SNAPSHOT.jar", "-w"]