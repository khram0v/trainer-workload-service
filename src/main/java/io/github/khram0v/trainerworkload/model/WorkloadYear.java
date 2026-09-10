package io.github.khram0v.trainerworkload.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkloadYear {

    @Positive
    private int year;

    @Valid
    private List<WorkloadMonth> months = new ArrayList<>();

    public WorkloadYear(int year) {
        this.year = year;
    }
}
