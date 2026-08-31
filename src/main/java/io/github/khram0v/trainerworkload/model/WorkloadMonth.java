package io.github.khram0v.trainerworkload.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workload_months")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkloadMonth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workload_year_id", nullable = false)
    private WorkloadYear workloadYear;

    @Column(nullable = false)
    private int month;

    @Column(name = "training_summary_duration", nullable = false)
    private int trainingSummaryDuration;

    public WorkloadMonth(WorkloadYear workloadYear, int month) {
        this.workloadYear = workloadYear;
        this.month = month;
        this.trainingSummaryDuration = 0;
    }
}
