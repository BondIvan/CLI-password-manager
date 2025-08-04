## Application for password management using command line interface
***

In this project I tried to stick to the principles of clean architecture.
The project is divided into the following layers: 
* UI (Presentation)
* Use Cases (Business Logic)
* Domain.

Some design patterns that were used:
* Strategy Pattern
* DTO & Mapper Pattern:
* Template method
* Repository Pattern

The data storage project does not use a database.
Instead, all data is stored in files on the user’s computer. Data files are created with limited access rights (the user who uses the system has the right to manage these files).

After launching the application will prompt you to enter a master password, which must be entered every time the application is launched.

You can use the "help" command to get information about all commands. For more detailed information about the command, you can add an argument to the "help [command name]" command.

#### Technology Stack:
* Java 21
* Maven
* Spring boot 3
* Spring shell
* Spring cache (ehcache)
* Bouncy Castle (Argon2)
* Logging - SLF4J and Logback
* Lombok
* Jackson

##### The application is under development.

***

### Getting Started
* Prerequisites:
  * Java (JDK) 21 or later
  * Apache Maven

* Building the Project
  * Clone the repository \
    ```git clone [https://github.com/BondIvan/CLI-password-manager]``` \
    ```cd cli-password-manager```
  * Build the executable JAR file using Maven \
    ```mvn clean package```
  * Running the Application \
    The application requires access to the system's graphical toolkit for clipboard functionality.
    Run the application from your terminal using the following command: \
  ```java -Djava.awt.headless=false -jar target/cli-password-manager-0.0.1-SNAPSHOT.jar```
    If the system does not have a graphical interface, you should change the ```-Djava.awt.headless``` flag to *true*