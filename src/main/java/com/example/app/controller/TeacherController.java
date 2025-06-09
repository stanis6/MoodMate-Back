package com.example.app.controller;

import com.example.app.dto.*;
import com.example.app.models.ConfirmationToken;
import com.example.app.models.User;
import com.example.app.repository.UserRepository;
import com.example.app.security.JwtUtil;
import com.example.app.service.*;
import com.example.app.sse.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teacher")
public class TeacherController {
    private final ClassroomService classroomService;
    private final UserService userService;
    private final SseService sseService;
    private final ReportService reportService;
    private final PdfReportService pdfReportService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @GetMapping("/classroom")
    public ResponseEntity<ClassroomDto> getClassroom(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        UUID teacherId = JwtUtil.extractUserId(jwt);
        ClassroomDto dto = classroomService.getClassroomByTeacherId(teacherId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/create-child")
    public ResponseEntity<?> createChildAccount(@RequestHeader("Authorization") String token, @RequestBody ChildAccountRequest request) {
        String jwtToken = token.substring(7);
        UUID userId = JwtUtil.extractUserId(jwtToken);

        userService.sendConfirmationEmail(userId, request.getParentEmail(), request.getChildFirstName(), request.getChildLastName());
        return ResponseEntity.ok("Confirmation email sent to parent. Please confirm to create the child's account.");
    }

    @GetMapping("/classroom/stream")
    public SseEmitter stream(@RequestParam("token") String token) {
        UUID teacherId = JwtUtil.extractUserId(token);
        return sseService.createEmitter(teacherId);
    }

    @GetMapping(
            value    = "/confirm",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> confirmChildAccount(@RequestParam("token") String token) {
        try {
            ConfirmationToken ct = userService.validateToken(token);
            userService.confirmToken(ct);

            User child = userService.createChildAccount(
                    ct.getUserId(),
                    ct.getParentEmail(),
                    ct.getChildFirstName(),
                    ct.getChildLastName()
            );

            ConfirmationResponse resp = new ConfirmationResponse(
                    child.getId(),
                    child.getUsername(),
                    child.getFirstName(),
                    child.getLastName()
            );
            return ResponseEntity.ok(resp);

        } catch (IllegalStateException e) {
            Map<String, String> err = Map.of("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PostMapping(
            value    = "/set-child-password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> setChildPassword(@RequestBody SetPasswordRequest req) {
        try {
            userService.updateChildPassword(req.getChildId(), req.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password updated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/report")
    public ResponseEntity<ChildReportDto> getChildReport(
            @RequestParam("childId") UUID childId,
            @RequestParam("date") String dateStr,
            Authentication authentication
    ) {
        LocalDate date = LocalDate.parse(dateStr);
        ChildReportDto dto = reportService.generateDailyReport(childId, date);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/report/all")
    public ResponseEntity<List<ChildReportDto>> getAllChildReports(
            @RequestParam("childId") UUID childId
    ) {
        return ResponseEntity.ok(reportService.getAllReports(childId));
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<ByteArrayResource> downloadReportPdf(
            @RequestParam("childId") UUID childId,
            @RequestParam("date") String dateStr
    ) {
        LocalDate date = LocalDate.parse(dateStr);
        ChildReportDto reportDto = reportService.generateDailyReport(childId, date);

        byte[] pdfBytes = pdfReportService.createPdf(reportDto);
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        String rawName = reportDto.getChildName().trim().replaceAll("\\s+", "_");
        String safeName = URLEncoder.encode(rawName, StandardCharsets.UTF_8);
        String filename = String.format("report_%s_%s.pdf", safeName, date);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
    }

    @PostMapping("/report/email")
    public ResponseEntity<?> emailReportToParent(
            @RequestParam("childId") UUID childId,
            @RequestParam("date") String dateStr
    ) {
        LocalDate date = LocalDate.parse(dateStr);

        ChildReportDto reportDto = reportService.generateDailyReport(childId, date);

        byte[] pdfBytes = pdfReportService.createPdf(reportDto);

        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalStateException("Child not found: " + childId));
        String parentEmail = child.getEmail();

        String rawName = reportDto.getChildName().trim().replaceAll("\\s+", "_");
        String safeName = URLEncoder.encode(rawName, StandardCharsets.UTF_8);
        String filename = String.format("report_%s_%s.pdf", safeName, date);

        String subject = "Raport Elev – " + reportDto.getChildName() + " (" + date + ")";
        String body    = "Bună ziua,\n\nAtașat găsiți raportul pentru "
                + reportDto.getChildName() + " din data " + date + ".\n\nO zi bună!";

        emailService.sendEmailWithAttachment(
                parentEmail,
                subject,
                body,
                filename,
                pdfBytes
        );

        return ResponseEntity.ok().body("{\"message\":\"Email trimis părintelui.\"}");
    }
}
