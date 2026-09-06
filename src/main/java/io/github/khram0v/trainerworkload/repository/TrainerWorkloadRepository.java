package io.github.khram0v.trainerworkload.repository;

import io.github.khram0v.trainerworkload.model.TrainerWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {

    Optional<TrainerWorkload> findByUsername(String username);

    List<TrainerWorkload> findByFirstNameAndLastName(String firstName, String lastName);
}
