package ryanair.task2.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Leg {
    private String departureAirport;
    private String arrivalAirport;
    private String departureDateTime;
    private String arrivalDateTime;

    public Leg() {
    }

}
