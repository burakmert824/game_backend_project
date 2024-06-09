# Backend Engineering Case Study

This project is designed to demonstrate a backend implementation for a game backend application using Spring Boot and MySQL. The application allows users to participate in tournaments, track their progress, and claim prizes based on their performance. The project includes RESTful APIs for managing users, tournaments, and their interactions.

Structure:
Controller Layer: Handles HTTP requests and responses.
Service Layer: Contains business logic.
Repository Layer: Interacts with the database.
Entity Layer: Defines the data model.

My database has three tables:
- users for holding user information (id, username, country, coins, level).
- tournaments for holding tournament information (id, date, is_started).
- user_tournament for holding the relationship between users and tournaments (user_id, tournament_id, is_claimed, score).
Every day new tournament starts tournament has different groups but their date are the same. User can have the same username but all of their id is unique.


Design Choices:
Layered Architecture is quite useful for developing complicated projects because every layer has a different job and they can be reused. Also, it makes the codebase easier to maintain and extend.
I want to make a stable response that always returns a JSON that includes data and message, this helps the receiver to know what is going to happen even if there is an error.
I tried to write custom exceptions, in that way, I could see the exceptions more easily and it helped me to maintain exceptions.
I tried to write unit tests to ensure that my system was working correctly.
I tried to write documented code to create easy to understand codebase.

In this project, I tried to write with clean architecture, stick to the separation of concerns, and do best practices for the backend to make my project more maintainable.




Some problems that I encountered:
  Claiming the reward was a problem because everyone was not claiming the reward; therefore, it was not a blocking thing for the ones who didn't win the tournament. To solve this, every time I checked for the unclaimed tournament I also checked if the user was not the first or the second one. Then, I set the user's tournament relationship's isClaimed field to true because it shouldn't be a blocker for them. This helped me to maintain the ones that are not the winner's isClaimed variable.
  Calculating the ranking was a problem because it should find the group every time before calculating the ranking. Instead, I could have stored the ranking data in the user-tournament relationship. But in this alternative, I had to dynamically change the ranking while the tournaments continued, which is a problem. I didn't choose this alternative for my implementation because checking the whole group every time was easy to implement and it would give the true real-time answer every time.
  I could use some caching strategies to hold the data so that the same SQL query would not have been sent multiple times.
  I had trouble testing static functions for getting the date and time. To fix this and make the code testable, I made a Clock bean and used it everywhere I needed the date and time. Then, I mocked the Clock in my tests. This way, I didn't need to mock static functions.
  I had trouble displaying data when the same request returned different types of responses depending on the situation. For example, increasing the level should return a score if the user is in a tournament, but it shouldn't return a score if the user is not in a tournament. To solve this problem, I created a stable response format. If a field was not relevant to the response, I returned null for that field.
