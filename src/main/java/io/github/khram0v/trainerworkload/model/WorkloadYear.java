package io.github.khram0v.trainerworkload.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "workload_years")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkloadYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trainer_username", nullable = false)
    private TrainerWorkload trainerWorkload;

    @Column(nullable = false)
    private int year;

    @OneToMany(mappedBy = "workloadYear", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorkloadMonth> months = new HashSet<>();

    public WorkloadYear(TrainerWorkload trainerWorkload, int year) {
        this.trainerWorkload = trainerWorkload;
        this.year = year;
    }
}
