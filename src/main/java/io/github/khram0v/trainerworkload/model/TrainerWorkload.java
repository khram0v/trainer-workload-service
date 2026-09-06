package io.github.khram0v.trainerworkload.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "trainer_workloads")
@CompoundIndex(name = "trainer_name_idx", def = "{'firstName' : 1, 'lastName' : 1}")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainerWorkload {

    @Id
    @NotBlank
    private String username;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private boolean active;

    @Valid
    private List<WorkloadYear> years = new ArrayList<>();

    public TrainerWorkload(String username, String firstName, String lastName, boolean active) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }
}
