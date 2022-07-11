package ryanair.task2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ryanair.task2.model.Interconnections;
import ryanair.task2.model.Route;
import ryanair.task2.model.Schedule;

import java.text.ParseException;
import java.util.List;

public interface IInterconnectionsService {

    List<Route> processRoutes(String routesRaw, String departure, String arrival) throws JsonProcessingException;

    List<List<Schedule>> processSchedules(List<List<String>> schedules, String departure, String arrival, String year, String month) throws JsonProcessingException, ParseException;

    List<Interconnections> processInterconnections(String departure, String arrival, List<String> connections, List<List<Schedule>> availableFlights, List<Route> availableRoutes) throws ParseException;

    List<String> processConnectingAirports(String result, String departureAirport, String arrivalAirport) throws JsonProcessingException;
}
