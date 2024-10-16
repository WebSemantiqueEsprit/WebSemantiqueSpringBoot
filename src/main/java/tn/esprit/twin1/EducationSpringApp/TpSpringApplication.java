package tn.esprit.twin1.EducationSpringApp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tn.esprit.twin1.EducationSpringApp.entities.Role;
import tn.esprit.twin1.EducationSpringApp.entities.User;
import tn.esprit.twin1.EducationSpringApp.repositories.UserRepository;
import tn.esprit.twin1.EducationSpringApp.servicesJena.ContractService;

@EnableScheduling
@EnableAspectJAutoProxy
@SpringBootApplication
@EnableAsync
public class TpSpringApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContractService jena; // Inject the Jena component

    public static void main(String[] args) {
        SpringApplication.run(TpSpringApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        User adminAccount = userRepository.findByRole(Role.ADMIN);
        if (adminAccount == null) {
            User user = new User();
            user.setEmail("admin@gmail.com");
            user.setFirstName("admin");
            user.setLastName("admin");
            user.setRole(Role.ADMIN);
            user.setPassword(new BCryptPasswordEncoder().encode("admin"));
            userRepository.save(user);
        }


    }
}
