package ryanair.task2.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class Route {

    private String airportFrom;
    private String airportTo;
    private String connectingAirport;
    private Boolean newRoute;
    private Boolean seasonalRoute;
    private String operator;
    private String carrierCode;
    private String group;
    private List similarArrivalAirportCodes = new ArrayList();
    private List tags = new ArrayList<>();

    public Route() {
    }
}
