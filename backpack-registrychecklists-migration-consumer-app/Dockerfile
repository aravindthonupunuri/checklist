FROM docker.target.com/tap/alpine-openjdk11-build:latest
ENV JAVA_HEAP_PERCENT="70"
ADD backpack-registrychecklists-migration-consumer-app/build/distributions/backpack-registrychecklists-migration-consumer-app.tar /
ENTRYPOINT ["/backpack-registrychecklists-migration-consumer-app/bin/backpack-registrychecklists-migration-consumer-app"]
