# Backend Engineering Case Study

Add brief explanation of how you organized your implementation and the choices you made in terms of design while solving problems


This project is designed to demonstrate a backend implementation for a game backend application using Spring Boot and MySQL. The application allows users to participate in tournaments, track their progress, and claim prizes based on their performance. The project includes RESTful APIs for managing users, tournaments, and their interactions.

Structre:
Controller Layer: Handles HTTP requests and responses.
Service Layer: Contains business logic.
Repository Layer: Interacts with the database.
Entity Layer: Defines the data model.

Design Choices:
Layered Architecture is very helpful to develop complicated projects because every layer has a different job and they can be reused. Also, it makes the codebase easier to maintain and extend.
I want to make a stable response that always return a json that includes data and message, in that way reciever can know what is going to happen even if there is an error.
I try to write custom exceptions, in that way I see the exceptions more easily and it helped me to maintain exceptions.
I tried to write unit tests to ensure that my system is working correctly.

In this project I tried to write with clean architecture and try to stick seperation of concerns and tried to do best practices for backend to make my project more maintainable.



