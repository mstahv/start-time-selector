# start-time-selector

Start Time Selector service for orienteering competitions. Originally written for FinnSpring 2019, since become popular for many competitions in Finland, partly thanks to Covid 19.

IRMA files, mentioned in the admin UI, are CSV files like these: https://irma.suunnistusliitto.fi/irma/haku/ilmoittautumiset?kilpailu=1208011&sarja=&seura=&kayttaja= 

Built with: 

 * Spring Boot
 * Hibernate
 * [Vaadin](https://vaadin.com)

To develop this further or try locally how it works:

 * Install JDK 17
 * Install Maven and/or Java IDE such as IntelliJ IDEA
 * Clone the project to your local workstation, run the Application class from your IDE or execute `mvn spring-boot:run` from CLI.
 * App will be running locally at http://localhost:8080/ and the more interesting admin part in http://localhost:8080/admin

If you want to use it by yourself, feel free to check out the app and host it or ask me for a sponsored hosting. It costs me ~ 4 € per month so you owe me nothing (or maybe a beer on the return trip from 25 Manna trip if that ever happens again), but neither do I nothing to you ;-)
