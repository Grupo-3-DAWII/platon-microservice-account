package edu.cibertec.account.service;

import edu.cibertec.account.dto.AccountRequest;
import edu.cibertec.account.dto.AccountResponse;
import edu.cibertec.account.dto.CustomerRegistrationRequest;
import edu.cibertec.account.repository.AccountRepository;
import edu.cibertec.account.repository.RoleRepository;
import edu.cibertec.account.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CustomerRegistrationService {
    private final AccountService accounts;
    private final AccountRepository repository;
    private final RoleRepository roles;
    private final StatusRepository statuses;

    @Transactional
    public AccountResponse register(CustomerRegistrationRequest request) {
        String name = text(request.name(), "Nombres", 80);
        String lastname = text(request.lastname(), "Apellidos", 80);
        if (!name.matches("[\\p{L}\\p{M}]+(?:[ '\u2019-][\\p{L}\\p{M}]+)*") ||
                !lastname.matches("[\\p{L}\\p{M}]+(?:[ '\u2019-][\\p{L}\\p{M}]+)*")) {
            throw new IllegalArgumentException("Nombres y apellidos: solo letras.");
        }
        String username = text(request.username(), "Usuario", 80).toLowerCase(Locale.ROOT);
        if (!username.matches("[a-z0-9._-]{3,80}")) throw new IllegalArgumentException("Usuario inválido. Usa al menos 3 caracteres.");
        String email = text(request.email(), "Correo", 120).toLowerCase(Locale.ROOT);
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}")) throw new IllegalArgumentException("Correo inválido.");
        String contact = text(request.nro(), "Contacto", 20);
        if (!contact.matches("\\+?[0-9 ()-]+")) throw new IllegalArgumentException("Contacto inválido.");
        contact = contact.replaceAll("\\D", "");
        if (contact.length() < 7 || contact.length() > 15) throw new IllegalArgumentException("Contacto: usa 7 a 15 dígitos.");
        String address = text(request.direccion(), "Dirección", 180);
        String password = request.password();
        if (password == null || password.isBlank() || password.length() < 10 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Contraseña: mínimo 10 caracteres y máximo 72 bytes.");
        }
        if (repository.hasUsername(username)) throw new IllegalArgumentException("El usuario ya está registrado.");
        if (repository.hasEmail(email)) throw new IllegalArgumentException("El correo ya está registrado.");
        if (repository.hasContact(contact)) throw new IllegalArgumentException("El contacto ya está registrado.");
        var customerRole = roles.findAll().stream().filter(role -> "USER".equalsIgnoreCase(role.name())).findFirst()
                .orElseThrow(() -> new IllegalStateException("El rol de cliente USER no está configurado"));
        var active = statuses.findAll().stream().filter(status -> "active".equalsIgnoreCase(status.getName())).findFirst()
                .orElseThrow(() -> new IllegalStateException("El estado activo no está configurado"));
        return accounts.create(new AccountRequest(name, lastname, username, password, customerRole.idRole(), email,
                contact, address, active.getIdStatus()));
    }

    private String text(String value, String field, int max) {
        if (value == null) throw new IllegalArgumentException(field + ": campo obligatorio.");
        String clean = Normalizer.normalize(value, Normalizer.Form.NFC).trim().replaceAll("\\s+", " ");
        if (clean.isBlank() || clean.length() > max || clean.matches(".*[\\p{Cntrl}\\p{Cf}].*")) {
            throw new IllegalArgumentException(field + ": revisa este campo.");
        }
        return clean;
    }
}
