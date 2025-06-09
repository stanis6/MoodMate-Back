package com.example.app.service;

import com.example.app.dto.ChildDto;
import com.example.app.events.ChildCreatedEvent;
import com.example.app.exception.EmailAlreadyExistsException;
import com.example.app.exception.InvalidCredentialsException;
import com.example.app.exception.UserAlreadyExistsException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.Classroom;
import com.example.app.models.ConfirmationToken;
import com.example.app.models.User;
import com.example.app.models.enums.UserRole;
import com.example.app.repository.ClassroomRepository;
import com.example.app.repository.ConfirmationTokenRepository;
import com.example.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final ConfirmationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;

    public User register(String firstName, String lastName, String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(UserRole.TEACHER);

        User savedUser = userRepository.save(user);

        // Create a classroom for the teacher
        Classroom classroom = new Classroom();
        classroom.setTeacherId(savedUser.getId());
        classroom.setName(firstName + " " + lastName + "'s Classroom");
        classroomRepository.save(classroom);

        return savedUser;
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return user;
    }

    public void sendConfirmationEmail(UUID teacherId,
                                      String parentEmail,
                                      String childFirstName,
                                      String childLastName) {
        String token = UUID.randomUUID().toString();
        ConfirmationToken ct = new ConfirmationToken();
        ct.setToken(token);
        ct.setUserId(teacherId);
        ct.setParentEmail(parentEmail);
        ct.setChildFirstName(childFirstName);
        ct.setChildLastName(childLastName);
        ct.setCreatedAt(LocalDateTime.now());
        ct.setExpiresAt(LocalDateTime.now().plusHours(24));
        ct.setConfirmed(false);
        tokenRepository.save(ct);

        String frontEndLink = "http://localhost:4200/confirm?token=" + token;

        String htmlBody =
                "<!DOCTYPE html>" +
                        "<html lang=\"en\">" +
                        "<head><meta charset=\"UTF-8\"></head>" +
                        "<body style=\"margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f4f4f7;\">" +
                        "  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">" +
                        "    <tr>" +
                        "      <td align=\"center\">" +
                        "        <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:40px auto;background:#ffffff;border-radius:8px;overflow:hidden;\">" +
                        "          <tr>" +
                        "            <td style=\"padding:40px;text-align:center;color:#2c3e50;\">" +
                        "              <h2 style=\"margin:0 0 16px;font-size:24px;\">Confirm Child Account</h2>" +
                        "              <p style=\"margin:0 0 24px;font-size:16px;\">" +
                        "                Please confirm the creation of <strong>" +
                        childFirstName + " " + childLastName +
                        "                </strong>'s account by clicking the button below." +
                        "              </p>" +
                        "              <a href=\"" + frontEndLink + "\"" +
                        "                 style=\"" +
                        "                   display:inline-block;" +
                        "                   padding:14px 32px;" +
                        "                   background-color:#34495e;" +
                        "                   color:#ffffff !important;" +
                        "                   text-decoration:none;" +
                        "                   font-size:16px;" +
                        "                   border-radius:4px;" +
                        "                   font-weight:bold;" +
                        "                 \">" +
                        "                Confirm Account" +
                        "              </a>" +
                        "            </td>" +
                        "          </tr>" +
                        "          <tr>" +
                        "            <td style=\"padding:20px;text-align:center;font-size:12px;color:#7f8c8d;\">" +
                        "              If you did not request this, you can safely ignore this email." +
                        "            </td>" +
                        "          </tr>" +
                        "        </table>" +
                        "      </td>" +
                        "    </tr>" +
                        "  </table>" +
                        "</body>" +
                        "</html>";

        emailService.sendHtmlEmail(
                parentEmail,
                "Please confirm your child’s account",
                htmlBody
        );
    }

    public ConfirmationToken validateToken(String token) {
        Optional<ConfirmationToken> optionalToken = tokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) {
            throw new IllegalStateException("Token not found");
        }

        ConfirmationToken confirmationToken = optionalToken.get();
        if (confirmationToken.isConfirmed()) {
            throw new IllegalStateException("Account is already confirmed");
        }

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token expired");
        }

        return confirmationToken;
    }

    public void confirmToken(ConfirmationToken token) {
        token.setConfirmed(true);
        tokenRepository.save(token);
    }

    public User createChildAccount(UUID userId,
                                   String parentEmail,
                                   String childFirstName,
                                   String childLastName) {
        Classroom classroom = classroomRepository.findByTeacherId(userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Classroom not found for teacher"));

        String firstNormalized = stripDiacritics(childFirstName).toLowerCase();
        String lastNormalized  = stripDiacritics(childLastName).toLowerCase();

        String[] firstTokens = firstNormalized.split("[\\s-]+");
        String[] lastTokens  = lastNormalized .split("[\\s-]+");
        String baseUsername  = firstTokens[0] + "_" + lastTokens[0];

        String username = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            suffix++;
            username = baseUsername + suffix;
        }

        User child = new User();
        child.setFirstName(childFirstName);
        child.setLastName(childLastName);
        child.setUsername(username);
        child.setPassword(passwordEncoder.encode("defaultPassword"));
        child.setEmail(parentEmail);
        child.setRole(UserRole.CHILD);
        child.setClassroom(classroom);

        child = userRepository.save(child);

        ChildDto dto = new ChildDto();
        dto.setId(child.getId());
        dto.setFirstName(child.getFirstName());
        dto.setLastName(child.getLastName());
        dto.setUsername(child.getUsername());
        events.publishEvent(new ChildCreatedEvent(this, userId, dto));

        return child;
    }

    private static final Pattern NON_SPACING_MARKS =
            Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private String stripDiacritics(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutMarks = NON_SPACING_MARKS.matcher(normalized).replaceAll("");
        return withoutMarks
                .replace('ș','s').replace('Ș','S')
                .replace('ț','t').replace('Ț','T');
    }

    public void updateChildPassword(UUID childId, String rawPassword) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        child.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(child);
    }
}