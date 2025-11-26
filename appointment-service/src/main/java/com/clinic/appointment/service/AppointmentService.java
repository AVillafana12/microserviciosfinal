package com.clinic.appointment.service;

import com.clinic.appointment.model.Appointment;
import com.clinic.appointment.model.AppointmentStatus;
import com.clinic.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public Appointment createAppointment(String patientId, String patientName, String doctorId, 
                                        String doctorName, String specialty, LocalDateTime appointmentDate, 
                                        String description) {
        Appointment appointment = Appointment.builder()
                .id(UUID.randomUUID().toString())
                .patientId(patientId)
                .patientName(patientName)
                .doctorId(doctorId)
                .doctorName(doctorName)
                .specialty(specialty)
                .appointmentDate(appointmentDate)
                .status(AppointmentStatus.SCHEDULED)
                .description(description)
                .build();
        
        return appointmentRepository.save(appointment);
    }

    public Appointment getAppointmentById(String appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));
    }

    public List<Appointment> listAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentsByPatient(String patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getAppointmentsByDoctor(String doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public Appointment cancelAppointment(String appointmentId, String reason) {
        Appointment appointment = getAppointmentById(appointmentId);
        
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed appointment");
        }
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setDescription(reason != null ? reason : appointment.getDescription());
        
        return appointmentRepository.save(appointment);
    }

    public Appointment confirmAppointment(String appointmentId) {
        Appointment appointment = getAppointmentById(appointmentId);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointmentRepository.save(appointment);
    }

    public Appointment completeAppointment(String appointmentId) {
        Appointment appointment = getAppointmentById(appointmentId);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAppointmentsByPatientAndStatus(String patientId, AppointmentStatus status) {
        return appointmentRepository.findByPatientIdAndStatus(patientId, status);
    }

    public List<Appointment> getAppointmentsByDoctorAndStatus(String doctorId, AppointmentStatus status) {
        return appointmentRepository.findByDoctorIdAndStatus(doctorId, status);
    }
}
