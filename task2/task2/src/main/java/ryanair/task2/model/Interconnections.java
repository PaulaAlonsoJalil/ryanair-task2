package ryanair.task2.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Interconnections {
    private Integer stops;
    private List<Leg> legs;

    public Interconnections() {
    }


}
