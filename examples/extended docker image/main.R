print("I am the extended docker image!")
people = read.csv2("input/people.csv", header = TRUE)
people$bmi <- people$mass/(people$size^2)
write.csv2(people, "output/people.csv", row.names = FALSE)