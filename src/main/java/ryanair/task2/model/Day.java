package ryanair.task2.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Day {
    private Integer day;
    private List<Flight> flights = new ArrayList<>();

    public Day() {
    }
}
