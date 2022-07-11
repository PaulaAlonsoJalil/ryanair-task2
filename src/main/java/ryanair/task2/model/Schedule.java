package ryanair.task2.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class Schedule {
    private Integer month;
    private List<Day> days = new ArrayList<>();

    public Schedule() {
    }
}
