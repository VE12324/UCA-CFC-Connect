package sv.edu.udb.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sv.edu.udb.model.Rol;
import sv.edu.udb.model.Usuario;
import sv.edu.udb.repository.RolRepository;
import sv.edu.udb.repository.UsuarioRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Rol adminRol = rolRepository.findByNombre("ADMIN").orElseGet(() -> {
                Rol r = new Rol();
                r.setNombre("ADMIN");
                r.setDescripcion("Administrador del sistema");
                return rolRepository.save(r);
            });

            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setEmail("admin@uca.edu.sv");
            admin.setPassword(passwordEncoder.encode("Admin@2024!"));
            admin.setRol(adminRol);
            usuarioRepository.save(admin);

            log.info("Usuario administrador creado: admin@uca.edu.sv / Admin@2024!");
        }
    }
}
