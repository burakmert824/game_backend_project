# Backend Engineering Case Study

This project is designed to demonstrate a backend implementation for a game backend application using Spring Boot and MySQL. The application allows users to participate in tournaments, track their progress, and claim prizes based on their performance. The project includes RESTful APIs for managing users, tournaments, and their interactions.

Structure:
Controller Layer: Handles HTTP requests and responses.
Service Layer: Contains business logic.
Repository Layer: Interacts with the database.
Entity Layer: Defines the data model.

Database explanation:
My database has three table: 
users for holding user information(id,username,country,coin,level),
tournaments for holding torunament's groups information (id,date,flag for started or not)
userTournament for holding the relationship between the users and tournaments(user_id,tournament_id, flag for claiming the revard, score of the tournament)


Design Choices:
Layered Architecture is quite useful for developing complicated projects because every layer has a different job and they can be reused. Also, it makes the codebase easier to maintain and extend.
I want to make a stable response that always returns a JSON that includes data and message, this helps the receiver to know what is going to happen even if there is an error.
I tried to write custom exceptions, in that way, I could see the exceptions more easily and it helped me to maintain exceptions.
I tried to write unit tests to ensure that my system was working correctly.

In this project, I tried to write with clean architecture, stick to the separation of concerns, and do best practices for the backend to make my project more maintainable.




Some problems that I encountered:
  Claiming the reward was a problem because everyone was not claiming the reward; therefore, it was not a blocking thing for the ones who didn't win the tournament. To solve this, every time I checked for the unclaimed tournament I also checked if the user was not the first or the second one. Then, I set the user's tournament relationship's isClaimed field to true because it shouldn't be a blocker for them. This helped me to maintain the ones that are not the winner's isClaimed variable.
  Calculating the ranking was a problem because it should find the group every time before calculating the ranking. Instead, I could have stored the ranking data in the user-tournament relationship. But in this alternative, I had to dynamically change the ranking while the tournaments continued, which is a problem. I didn't choose this alternative for my implementation because checking the whole group every time was easy to implement and it would give the true real-time answer every time.
  I could use some caching strategies to hold the data so that the same SQL query would not have been sent multiple times.
  I had trouble testing static functions for getting the date and time. To fix this and make the code testable, I made a Clock bean and used it everywhere I needed the date and time. Then, I mocked the Clock in my tests. This way, I didn't need to mock static functions.


