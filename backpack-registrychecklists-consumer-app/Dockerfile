FROM docker.target.com/tap/alpine-openjdk11-build:latest
ENV JAVA_HEAP_PERCENT="70"
ADD backpack-registrychecklists-consumer-app/build/distributions/backpack-registrychecklists-consumer-app.tar /
ENTRYPOINT ["/backpack-registrychecklists-consumer-app/bin/backpack-registrychecklists-consumer-app"]
