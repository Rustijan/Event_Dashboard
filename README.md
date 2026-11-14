# EventHub Final Project — Student Starter

Build a Java 25 Spring Boot MVC application for event organizers and participants. Read the complete specification in [Task_EN.md](Task_EN.md) or [Task_RU.md](Task_RU.md) before you begin.

The starter contains the Maven configuration, Spring Boot entry point, Thymeleaf templates and CSS, ready-to-use PostgreSQL scripts, and an open acceptance-test suite. You implement the Java model, DTOs, interfaces and implementations, JDBC repositories, services, controllers, access filter, exception handling, and your own service unit tests. Keep the supplied templates, SQL scripts, and acceptance tests unchanged.

The starter initially contains only the application entry point under `src/main/java`. Therefore, the supplied tests will not compile until you create the required contracts and classes described in the specification. This is expected: the tests are an executable description of the target behavior.

## Requirements

- JDK 25;
- PostgreSQL;
- Internet access for the first Maven Wrapper run.

Check the selected JDK:

```bash
java -version
./mvnw -version
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Database setup

Run the following command once as a PostgreSQL administrator:

```bash
psql -U postgres -f database/create-database.sql
```

Then deploy the provided schema and demonstration data as the application user:

```bash
psql -U eventhub_user -d eventhub -f database/schema.sql
psql -U eventhub_user -d eventhub -f database/data.sql
```

The local learning credentials are:

```text
DB_URL=jdbc:postgresql://localhost:5432/eventhub
DB_USERNAME=eventhub_user
DB_PASSWORD=eventhub_password
```

The demonstration accounts use password `Password123!`:

| Role | Email |
| --- | --- |
| Organizer | `organizer@eventhub.local` |
| Participant | `participant@eventhub.local` |

Dates in `data.sql` are relative to the moment when the script is run, so the demonstration sessions remain in the future. Running `schema.sql` again removes existing EventHub data; run `data.sql` afterward to restore the demonstration set.

## Build and run

```bash
./mvnw test
./mvnw spring-boot:run
```

The test suite uses an H2 in-memory database and does not require the local PostgreSQL server. Its test records mirror the supplied demonstration dataset, and all session dates are calculated relative to the current test run.

Open [http://localhost:8080/events](http://localhost:8080/events).

Do not commit real deployment passwords. The supplied password is only for a local learning environment.
