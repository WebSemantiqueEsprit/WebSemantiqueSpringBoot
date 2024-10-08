package tn.esprit.twin1.EducationSpringApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.twin1.EducationSpringApp.entities.Foyer;

import java.util.List;
import java.util.Optional;

public interface FoyerRepositorie  extends JpaRepository<Foyer,Long> {
    Optional<Foyer> findByNomFoyer(String nomFoyer);


    @Query("SELECT f.nomFoyer FROM Foyer f")
    List<String> getAllNomsFoyer();


}
