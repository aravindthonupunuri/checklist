FROM docker.target.com/tap/alpine-openjdk11-build:latest
ENV JAVA_HEAP_PERCENT="70"
ADD backpack-registry-consumer-app/build/distributions/backpack-registrychecklists-consumer-app.tar /
ENTRYPOINT ["/backpack-registry-consumer-app/bin/backpack-registrychecklists-consumer-app"]
