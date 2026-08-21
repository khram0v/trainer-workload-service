package io.github.khram0v.trainerworkload.repository;

import io.github.khram0v.trainerworkload.model.TrainerWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkload, String> {

    @Query("""
            SELECT DISTINCT t FROM TrainerWorkload t
            LEFT JOIN FETCH t.years y
            LEFT JOIN FETCH y.months
            WHERE t.username = :username
            """)
    Optional<TrainerWorkload> findByUsernameWithYearsAndMonths(@Param("username") String username);
}
