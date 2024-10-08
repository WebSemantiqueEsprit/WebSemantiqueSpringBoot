package tn.esprit.twin1.EducationSpringApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.twin1.EducationSpringApp.entities.Bloc;

public interface BlocRepositorie  extends JpaRepository<Bloc,Long> {
    Bloc findByNomBloc(String nomBloc);
}
