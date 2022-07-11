package ryanair.task2.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import ryanair.task2.exceptions.BadRequestException;
import ryanair.task2.model.Interconnections;
import ryanair.task2.model.Route;
import ryanair.task2.model.Schedule;
import ryanair.task2.service.impl.InterconnectionsServiceImpl;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
@CrossOrigin
public class InterconnectionsController {

    @Autowired
    private InterconnectionsServiceImpl interconnectionsService;

    private static final Logger logger = Logger.getLogger(InterconnectionsController.class);


    //Consulta el endpoint de Ryanair de schedules y envía la información obtenida al service.
    private List<List<Schedule>> getSchedules(List<String> connections, String departure, String arrival, String departureDateTime, String arrivalDateTime) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            List<String> schedulesFromDepartureToArrival = new ArrayList<>();
            List<String> schedulesFromDepartureToConnection = new ArrayList<>();
            List<String> schedulesFromConnectionToArrival = new ArrayList<>();

            List<List<String>> schedules = new ArrayList<>(3);

            //Routes from the origin to the destiny
            String uri = "https://services-api.ryanair.com/timtbl/3/schedules/" + departure + "/" + arrival + "/years/" + interconnectionsService.getTimeInDateTime(departureDateTime).getYear() + "/months/" + interconnectionsService.getTimeInDateTime(departureDateTime).getMonthValue();
            String result1 = restTemplate.getForObject(uri, String.class);
            schedulesFromDepartureToArrival.add(result1);
            logger.info("Schedules from origin to destiny were successfully obtained from the API.");


            //Routes from the origin to each connection
            for (String connection : connections) {
                String uri2 = "https://services-api.ryanair.com/timtbl/3/schedules/" + departure + "/" + connection + "/years/" + interconnectionsService.getTimeInDateTime(departureDateTime).getYear() + "/months/" + interconnectionsService.getTimeInDateTime(departureDateTime).getMonthValue();
                String result2 = restTemplate.getForObject(uri2, String.class);
                schedulesFromDepartureToConnection.add(result2);
            }
            logger.info("Schedules from origin to connection were successfully obtained from the API.");
            //Routes from each connection to the destiny
            for (String connection : connections) {
                String uri3 = "https://services-api.ryanair.com/timtbl/3/schedules/" + connection + "/" + arrival + "/years/" + interconnectionsService.getTimeInDateTime(departureDateTime).getYear() + "/months/" + interconnectionsService.getTimeInDateTime(departureDateTime).getMonthValue();
                String result3 = restTemplate.getForObject(uri3, String.class);
                schedulesFromConnectionToArrival.add(result3);
            }
            logger.info("Schedules from connection to destiny were successfully obtained from the API.");

            schedules.add(schedulesFromDepartureToArrival);
            schedules.add(schedulesFromDepartureToConnection);
            schedules.add(schedulesFromConnectionToArrival);

            List<List<Schedule>> response = interconnectionsService.processSchedules(schedules, departure, arrival, departureDateTime, arrivalDateTime);

            return response;

        } catch (Exception e) {
            logger.error("Failed to get schedules from the API");
        }
        return null;
    }


    //Consulta las rutas, y envía la información para ser procesada por el service
    private List<String> getConnections(String departureAirport, String arrivalAirport) {
        try {
            String uri = "https://services-api.ryanair.com/locate/3/routes";
            RestTemplate restTemplate = new RestTemplate();

            String result = restTemplate.getForObject(uri, String.class);

            List<String> response = interconnectionsService.processConnectingAirports(result, departureAirport, arrivalAirport);
            logger.info("Connections were successfully obtained from the API.");

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to get connections from the API");
        }
        return null;
    }

    //Consulta el endpoint Ryanair de rutas y envía la información obtenida al service
    private List<Route> getRoutes(String departureAirport, String arrivalAirport) {
        try {
            String uri = "https://services-api.ryanair.com/locate/3/routes";
            RestTemplate restTemplate = new RestTemplate();

            String result = restTemplate.getForObject(uri, String.class);

            List<Route> response = interconnectionsService.processRoutes(result, departureAirport, arrivalAirport);

            logger.info("Routes were successfully obtained from the API.");

            return response;
        } catch (Exception e) {
            logger.error("Failed to get Routes from the API.");
        }
        return null;
    }

    //Finds flights interconnections
    @GetMapping(value = {"/interconnections"})
    public ResponseEntity<?> findInterconnections(@RequestParam String departure, @RequestParam String arrival, @RequestParam String departureDateTime, @RequestParam String arrivalDateTime) {
        try {

            //Calls getConnections
            List<String> connections = getConnections(departure, arrival);
            //Calls getSchedules
            List<List<Schedule>> availableFlights = getSchedules(connections, departure, arrival, departureDateTime, arrivalDateTime);
            if (availableFlights == null) {
                logger.error("Failed to get Schedules.");
                throw new BadRequestException("result from getSchedules method is null.");
            }
            //Calls getRoutes
            List<Route> availableRoutes = getRoutes(departure, arrival);
            //Sends the information to the service
            List<Interconnections> result = interconnectionsService.processInterconnections(departure, arrival, connections, availableFlights, availableRoutes);

            logger.info("All connections were successfully sent to the Service.");

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get Interconnections.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
