# extend the r-microservice
#FROM r-microservice
FROM registry.gitlab.com/debuglevel/r-microservice:latest

# add the "static" R scripts to the docker image
ADD main.R /app

# point the environment variable to the R file to be executed
ENV R_MAINFILE=/app/main.R