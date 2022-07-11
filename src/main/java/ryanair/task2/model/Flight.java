package ryanair.task2.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class Flight {
    private String carrierCode;
    private String number;
    private String departureTime;
    private String arrivalTime;

    public Flight() {
    }


}
