package ryanair.task2.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ryanair.task2.model.*;
import ryanair.task2.service.IInterconnectionsService;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class InterconnectionsServiceImpl implements IInterconnectionsService {

    @Autowired
    private ObjectMapper objectMapper;

    protected final static Logger logger = Logger.getLogger(InterconnectionsServiceImpl.class);


    @Override
    public List<Route> processRoutes(String routesRaw, String departure, String arrival) throws JsonProcessingException {

        List<Route> allRoutes = objectMapper.readValue(routesRaw, new TypeReference<>() {
        });

        allRoutes.removeIf(route -> route.getConnectingAirport() != null || !route.getOperator().equals("RYANAIR"));

        allRoutes.removeIf(processedRoute -> !Objects.equals(processedRoute.getAirportFrom(), departure) && !Objects.equals(processedRoute.getAirportTo(), arrival));

        List<Route> processedRoutes = new ArrayList<>();

        for (Route allRoute : allRoutes) {
            //if the origin and destination are the same, it is added to a new list
            if (allRoute.getAirportFrom().equals(departure) && allRoute.getAirportTo().equals(arrival)) {
                processedRoutes.add(allRoute);

                //if the departure is the same but the destination is not, search the original list to see if there is another route with that departure.
            } else if (allRoute.getAirportFrom().equals(departure) && !allRoute.getAirportTo().equals(arrival)) {
                String destination = allRoute.getAirportTo();
                for (Route route : allRoutes) {
                    if (route.getAirportFrom().equals(destination)) {
                        processedRoutes.add(route);
                        processedRoutes.add(allRoute);
                    }
                }
            }
        }

        logger.info("The routes where processed succesfully.");
        if (processedRoutes.isEmpty()) {
            logger.info("result from processRoutes method is null.");
        }
        return processedRoutes;

    }

    @Override
    public List<List<Schedule>> processSchedules(List<List<String>> schedules, String departure, String arrival, String departureDateTime, String arrivalDateTime) throws JsonProcessingException {

        List<String> schedulesFromDepartureToArrival = schedules.get(0);
        List<String> schedulesFromDepartureToConnection = schedules.get(1);
        List<String> schedulesFromConnectionToArrival = schedules.get(2);

        List<Schedule> schedulesFromDepartureToArrivalProcessed = new ArrayList<>();
        List<Schedule> schedulesFromDepartureToConnectionProcessed = new ArrayList<>();
        List<Schedule> schedulesFromConnectionToArrivalProcessed = new ArrayList<>();

        List<List<Schedule>> listOfAllSchedules = new ArrayList<>();


        for (String schedules1 : schedulesFromDepartureToArrival) {
            schedulesFromDepartureToArrivalProcessed.add(objectMapper.readValue(schedules1, new TypeReference<>() {
            }));
        }

        for (String schedules2 : schedulesFromDepartureToConnection) {
            schedulesFromDepartureToConnectionProcessed.add(objectMapper.readValue(schedules2, new TypeReference<>() {
            }));
        }
        for (String schedules3 : schedulesFromConnectionToArrival) {
            schedulesFromConnectionToArrivalProcessed.add(objectMapper.readValue(schedules3, new TypeReference<>() {
            }));
        }

        listOfAllSchedules.add(schedulesFromDepartureToArrivalProcessed);
        listOfAllSchedules.add(schedulesFromDepartureToConnectionProcessed);
        listOfAllSchedules.add(schedulesFromConnectionToArrivalProcessed);

        LocalDateTime formattedDepartureTime = getTimeInDateTime(departureDateTime);


        int index1 = 0;
        List<List<Schedule>> listOfAllValidSchedules = listOfAllSchedules;
        Day day1 = new Day();
        Flight flight1 = new Flight();

        for (List<Schedule> listOfAllSchedule : listOfAllSchedules) {
            int index2 = 0;
            for (Schedule schedule : listOfAllSchedule) {
                int index3 = 0;
                for (Day day : schedule.getDays()) {
                    if (day.getDay() != formattedDepartureTime.getDayOfMonth()) {
                        int index4 = 0;
                        for (Flight flight : day.getFlights()) {
                            if (getOnlyHoursAndMinutes(flight.getDepartureTime()).isBefore(getOnlyHoursAndMinutes(departureDateTime)) || getOnlyHoursAndMinutes(flight.getArrivalTime()).isAfter(getOnlyHoursAndMinutes(arrivalDateTime))) {
                                listOfAllValidSchedules.get(index1).get(index2).getDays().get(index3).getFlights().set(index4, flight1);
                            }
                            index4++;
                        }
                        listOfAllValidSchedules.get(index1).get(index2).getDays().set(index3, day1);
                    }
                    index3++;
                }
                index2++;

            }
            index1++;
        }

        logger.info("The schedules where processed succesfully.");
        if (listOfAllSchedules.isEmpty()) {
            logger.info("result from processSchedules method is null.");
        }
        return listOfAllSchedules;
    }

    @Override
    public List<Interconnections> processInterconnections(String departure, String arrival, List<String> connections, List<List<Schedule>> availableFlights, List<Route> availableRoutes) {

        List<Schedule> schedulesFromDepartureToArrival = availableFlights.get(0);
        List<Schedule> schedulesFromDepartureToConnection = availableFlights.get(1);
        List<Schedule> schedulesFromConnectionToArrival = availableFlights.get(2);

        //Cleansing of the lists
        schedulesFromDepartureToArrival.get(0).getDays().removeIf(day -> day.getDay() == null);

        for (int i = 0; i < schedulesFromDepartureToConnection.size(); i++) {
            schedulesFromDepartureToConnection.get(i).getDays().removeIf(day -> day.getDay() == null);
            schedulesFromDepartureToConnection.removeIf(element -> element.getDays().isEmpty());

        }
        for (int i = 0; i < schedulesFromDepartureToConnection.size(); i++) {
            if (schedulesFromDepartureToConnection.get(i).getMonth() == 0) {
                schedulesFromDepartureToConnection.remove(i);
            }
        }

        for (Schedule value : schedulesFromConnectionToArrival) {
            value.getDays().removeIf(day -> day.getDay() == null);
        }
        for (int i = 0; i < schedulesFromConnectionToArrival.size(); i++) {
            if (schedulesFromConnectionToArrival.get(i).getMonth() == 0) {
                schedulesFromConnectionToArrival.remove(i);
            }
        }

        //Assembling the objects
        Interconnections allInterconnectionsWithoutScales = new Interconnections();
        Interconnections interconnectionsWithoutScales;
        Leg leg;
        List<Leg> legs;

        for (Schedule schedule : schedulesFromDepartureToArrival) {
            interconnectionsWithoutScales = new Interconnections();

            legs = new ArrayList<>();
            interconnectionsWithoutScales.setStops(0);

            for (Day day : schedule.getDays()) {
                for (Flight flight : day.getFlights()) {
                    leg = new Leg();
                    leg.setArrivalDateTime(flight.getArrivalTime());
                    leg.setDepartureDateTime(flight.getDepartureTime());
                    leg.setDepartureAirport(departure);
                    leg.setArrivalAirport(arrival);
                    legs.add(leg);
                }
            }
            interconnectionsWithoutScales.setLegs(legs);

            allInterconnectionsWithoutScales = interconnectionsWithoutScales;
        }

        List<Interconnections> allInterconnectionsFromDepartureToConnection = new ArrayList<>();
        Interconnections interconnectionsFromDepartureToConnection;
        Leg leg1;
        List<Leg> legs1;
        for (Schedule schedule : schedulesFromDepartureToConnection) {
            interconnectionsFromDepartureToConnection = new Interconnections();

            legs1 = new ArrayList<>();
            interconnectionsFromDepartureToConnection.setStops(1);

            for (Day day : schedule.getDays()) {
                for (Flight flight : day.getFlights()) {
                    leg1 = new Leg();
                    leg1.setArrivalDateTime(flight.getArrivalTime());
                    leg1.setDepartureDateTime(flight.getDepartureTime());
                    leg1.setDepartureAirport(departure);
                    leg1.setArrivalAirport(arrival);
                    legs1.add(leg1);
                }
            }
            interconnectionsFromDepartureToConnection.setLegs(legs1);

            allInterconnectionsFromDepartureToConnection.add(interconnectionsFromDepartureToConnection);

        }

        List<Interconnections> allInterconnectionsFromConnectionToArrival = new ArrayList<>();
        Interconnections InterconnectionsFromConnectionToArrival;
        Leg leg2;
        List<Leg> legs2;
        for (Schedule schedule : schedulesFromConnectionToArrival) {
            InterconnectionsFromConnectionToArrival = new Interconnections();

            legs2 = new ArrayList<>();
            InterconnectionsFromConnectionToArrival.setStops(1);

            for (Day day : schedule.getDays()) {

                for (Flight flight : day.getFlights()) {
                    leg2 = new Leg();
                    leg2.setArrivalDateTime(flight.getArrivalTime());
                    leg2.setDepartureDateTime(flight.getDepartureTime());
                    leg2.setDepartureAirport(departure);
                    leg2.setArrivalAirport(arrival);

                    legs2.add(leg2);
                }
            }

            InterconnectionsFromConnectionToArrival.setLegs(legs2);

            if (!legs2.isEmpty()) {
                allInterconnectionsFromConnectionToArrival.add(InterconnectionsFromConnectionToArrival);
            }

        }


        List<Interconnections> allInterconnections = new ArrayList<>();
        allInterconnections.add(allInterconnectionsWithoutScales);
        allInterconnections.addAll(allInterconnectionsFromDepartureToConnection);
        allInterconnections.addAll(allInterconnectionsFromConnectionToArrival);

        logger.info("The interconnections where processed succesfully.");
        if (allInterconnections.isEmpty()) {
            logger.info("result from processInterconnections method is null.");
        }
        return allInterconnections;
    }

    @Override
    public List<String> processConnectingAirports(String routesRaw, String departure, String arrival) throws JsonProcessingException {


        List<Route> allRoutes = objectMapper.readValue(routesRaw, new TypeReference<>() {
        });

        allRoutes.removeIf(route -> route.getConnectingAirport() != null || !route.getOperator().equals("RYANAIR"));

        allRoutes.removeIf(processedRoute -> !Objects.equals(processedRoute.getAirportFrom(), departure) && !Objects.equals(processedRoute.getAirportTo(), arrival));

        List<String> connections = new ArrayList<>();


        for (Route allRoute : allRoutes) {

            if (allRoute.getAirportFrom().equals(departure) && !allRoute.getAirportTo().equals(arrival)) {
                String destination = allRoute.getAirportTo();
                connections.add(destination);
            }
        }
        return connections;
    }

    public LocalDateTime getTimeInDateTime(String dateTime) {

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);

        return LocalDateTime.parse(dateTime, inputFormatter);
    }


    public LocalDateTime getOnlyHoursAndMinutes(String date) {

        String hoursAndMinutes;
        if (date.length() > 5) {
            hoursAndMinutes = date.substring(11, 16);
            hoursAndMinutes = "0001-01-01T" + hoursAndMinutes;
        } else {
            hoursAndMinutes = "0001-01-01T" + date;
        }
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);

        return LocalDateTime.parse(hoursAndMinutes, inputFormatter);
    }

}
