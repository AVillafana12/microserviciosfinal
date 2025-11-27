package com.clinic.appointment.controller;

import com.clinic.appointment.model.Appointment;
import com.clinic.appointment.model.AppointmentStatus;
import com.clinic.appointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * POST /appointments - Crear nueva cita (solo doctores y admins)
     */
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody AppointmentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userRole = extractRole(auth);
        
        if (!userRole.equals("admin") && !userRole.equals("doctor")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Appointment appointment = appointmentService.createAppointment(
                request.getPatientId(),
                request.getPatientName(),
                request.getDoctorId(),
                request.getDoctorName(),
                request.getSpecialty(),
                LocalDateTime.parse(request.getAppointmentDate()),
                request.getDescription()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    /**
     * GET /appointments - Listar citas (filtradas por rol)
     * - Pacientes: ven sus propias citas
     * - Doctores: ven sus citas asignadas
     * - Admins: ven todas
     */
    @GetMapping
    public ResponseEntity<List<Appointment>> listAppointments(
            @RequestParam(required = false) AppointmentStatus status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = extractUserId(auth);
        String userRole = extractRole(auth);

        List<Appointment> appointments;

        if (userRole.equals("admin")) {
            appointments = appointmentService.listAllAppointments();
        } else if (userRole.equals("doctor")) {
            appointments = appointmentService.getAppointmentsByDoctor(userId);
        } else if (userRole.equals("patient")) {
            appointments = appointmentService.getAppointmentsByPatient(userId);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (status != null) {
            appointments = appointments.stream()
                    .filter(a -> a.getStatus() == status)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(appointments);
    }

    /**
     * GET /appointments/{id} - Obtener detalles de una cita (con validación de acceso)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointment(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = extractUserId(auth);
        String userRole = extractRole(auth);

        Appointment appointment = appointmentService.getAppointmentById(id);

        // Validar acceso
        if (userRole.equals("admin")) {
            // Admin puede ver cualquier cita
            return ResponseEntity.ok(appointment);
        } else if (userRole.equals("doctor")) {
            // Doctor solo ve sus citas
            if (!appointment.getDoctorId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(appointment);
        } else if (userRole.equals("patient")) {
            // Paciente solo ve sus citas
            if (!appointment.getPatientId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(appointment);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * GET /appointments/patient/{patientId} - Listar citas de un paciente (acceso restringido)
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByPatient(@PathVariable String patientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = extractUserId(auth);
        String userRole = extractRole(auth);

        // Solo admin o el mismo paciente pueden ver sus citas
        if (!userRole.equals("admin") && !userId.equals(patientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * GET /appointments/doctor/{doctorId} - Listar citas de un doctor (acceso restringido)
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctor(@PathVariable String doctorId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = extractUserId(auth);
        String userRole = extractRole(auth);

        // Solo admin o el mismo doctor pueden ver sus citas
        if (!userRole.equals("admin") && !userId.equals(doctorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Appointment> appointments = appointmentService.getAppointmentsByDoctor(doctorId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * PUT /appointments/{id}/confirm - Confirmar cita (solo doctores y admins)
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Appointment> confirmAppointment(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userRole = extractRole(auth);

        if (!userRole.equals("admin") && !userRole.equals("doctor")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Appointment appointment = appointmentService.confirmAppointment(id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * PUT /appointments/{id}/complete - Marcar cita como completada (solo doctores y admins)
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<Appointment> completeAppointment(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userRole = extractRole(auth);

        if (!userRole.equals("admin") && !userRole.equals("doctor")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Appointment appointment = appointmentService.completeAppointment(id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * DELETE /appointments/{id} - Cancelar cita (paciente o doctor puede cancelar sus propias citas)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Appointment> cancelAppointment(
            @PathVariable String id,
            @RequestParam(required = false) String reason) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = extractUserId(auth);
        String userRole = extractRole(auth);

        Appointment appointment = appointmentService.getAppointmentById(id);

        // Validar permisos: admin, o el paciente/doctor de la cita
        if (!userRole.equals("admin")) {
            if (userRole.equals("patient") && !appointment.getPatientId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (userRole.equals("doctor") && !appointment.getDoctorId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        Appointment cancelled = appointmentService.cancelAppointment(id, reason);
        return ResponseEntity.ok(cancelled);
    }

    /**
     * Extraer ID de usuario del token JWT
     */
    private String extractUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) auth.getPrincipal();
            return jwt.getClaimAsString("sub");
        }
        return null;
    }

    /**
     * Extraer rol del usuario del token JWT
     */
    private String extractRole(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) auth.getPrincipal();
            Object roles = jwt.getClaim("realm_access");
            if (roles instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> realmAccess = (Map<String, Object>) roles;
                Object rolesList = realmAccess.get("roles");
                if (rolesList instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> roleList = (List<String>) rolesList;
                    if (roleList.contains("admin")) return "admin";
                    if (roleList.contains("doctor")) return "doctor";
                    if (roleList.contains("patient")) return "patient";
                }
            }
        }
        return "anonymous";
    }

    /**
     * DTO para crear cita
     */
    @lombok.Data
    public static class AppointmentRequest {
        private String patientId;
        private String patientName;
        private String doctorId;
        private String doctorName;
        private String specialty;
        private String appointmentDate; // ISO 8601 format
        private String description;
    }
}
