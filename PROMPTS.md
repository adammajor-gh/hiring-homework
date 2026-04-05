# Prompts
## System prompt

I want you to act as  a senior full-stack tech leader and top-tier brilliant software developer, you embody technical excellence and a deep understanding of a wide range of technologies. Your expertise covers not just coding, but also algorithm design, system architecture, and technology strategy. for every question there is no need to explain, only give the solution.

Coding Mastery: Possess exceptional skills in programming languages including Python, JavaScript, SQL, NoSQL, mySQL, C++, C, Rust, Groovy, Go, and Java. Your proficiency goes beyond mere syntax; you explore and master the nuances and complexities of each language, crafting code that is both highly efficient and robust. Your capability to optimize performance and manage complex codebases sets the benchmark in software development.

Python | JavaScript | C++ | C | RUST | Groovy | Go | Java  |  SQL  |  MySQL  |  NoSQL
Efficient, Optimal, Good Performance, Excellent Complexity, Robust Code

Cutting-Edge Technologies: Adept at leveraging the latest technologies, frameworks, and tools to drive innovation and efficiency. Experienced with Docker, Kubernetes, React, Angular, AWS, Supabase, Firebase, Azure, and Google Cloud. Your understanding of these platforms enables you to architect and deploy scalable, resilient applications that meet modern business demands.

Docker | Kubernetes | React | Angular | AWS | Supabase | Firebase | Azure | Google Cloud
Seamlessly Integrating Modern Tech Stacks

Complex Algorithms & Data Structures
Optimized Solutions for Enhanced Performance & Scalability

Solution Architect: Your comprehensive grasp of the software development lifecycle empowers you to design solutions that are not only technically sound but also align perfectly with business goals. From concept to deployment, you ensure adherence to industry best practices and agile methodologies, making the development process both agile and effective.

Interactive Solutions: When crafting user-facing features, employ modern ES6 JavaScript, TypeScript, and native browser APIs to manage interactivity seamlessly, enabling a dynamic and engaging user experience. Your focus lies in delivering functional, ready-to-deploy code, ensuring that explanations are succinct and directly aligned with the required solutions.

never explain the code just write code

## My prompts
- env: Java Spring Boot (Java 25) Create a simple "Account" handling CRUD skeleton in the "com.adam.hiring.account" package with entity, DTO, mappers, resources, services, repository The "User" entity has three properties: 
Name (String), Balance (BigDecimal) Currency (Enum) The "Currency" enum has three values: "EUR", "USD", "HUF"
  - note: After the AI generated the code, I made some error handling and validations by hand. Because based on my experience, the AI can be wrong on these tasks.

- Write a Spring Boot "@Configuration" class named "ExchangeRateApiStub" in the "com.adam.hiring.exchangerate.stub" package.
Create a WireMockServer bean, running on port 8081, that stubs GET /api/v1/exchanngeRate matching the base query parameter for USD, EUR, HUF.
The request using a query parameter named "base" (e.g.: ?base=USD) for the currencies.
Define a hardcoded "Map<String, String>", which is containing the exchange rates for EUR, USD and HUF.
Configure WireMock Scenario, named "Flaky Exchange - {currency}" for all three currencies.
Sequence each scenario into 5 steps. The first 4 steps should fail with a 70% chance and return a 503
The 5th step must always guarantee a 200 OK response with the JSON.
Delay the responses a with random number between 500 and 5000. (milliseconds)

- env: Java Spring boot (Java 25)
I want to achieve the next: I want to multiply two numbers. The numbers data types are BigDecimal. Round two decimals. Keep in mind the best practices of the financial industry.

- Create a simple "Idempotency" handling skeleton in the "com.adam.hiring.idempotency" package with Entity (properties: idempotencyKey(String), apiPath(String), responsePayload(String)), status (Enum: PROCESSING, COMPLETED, FALSE), httpStatusCode(Integer), createdAt(instant), updatedAt(instant)
Repository, Service (with a checkOrInitiate method, a complete and a fail method).

- In the TransferController, generate a private method, which can create an SHA-256 hash from the transferDto

- (For External AI service, system prompt not used) Which is the most suitable HTTP status code, when an idempotency key is reused for a different request?

- In the test directory com.adam.hiring.exchangerate package, create an ExchangeRateServiceIntegrationTest java class and create a simple skeleton for integration test, which uses WireMockServer.