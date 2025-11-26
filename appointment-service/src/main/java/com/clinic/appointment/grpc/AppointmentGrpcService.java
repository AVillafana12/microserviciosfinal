package com.clinic.appointment.grpc;

import com.clinic.appointment.model.Appointment;
import com.clinic.appointment.model.AppointmentStatus;
import com.clinic.appointment.service.AppointmentService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import com.clinic.appointment.proto.*;

import java.time.LocalDateTime;
import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class AppointmentGrpcService extends AppointmentServiceGrpc.AppointmentServiceImplBase {

    private final AppointmentService appointmentService;

    @Override
    public void createAppointment(CreateAppointmentRequest request, 
                                StreamObserver<AppointmentResponse> responseObserver) {
        try {
            Appointment appointment = appointmentService.createAppointment(
                    request.getPatientId(),
                    request.getPatientName(),
                    request.getDoctorId(),
                    request.getDoctorName(),
                    request.getSpecialty(),
                    LocalDateTime.parse(request.getAppointmentDate()),
                    request.getDescription()
            );

            AppointmentResponse response = AppointmentResponse.newBuilder()
                    .setId(appointment.getId())
                    .setPatientId(appointment.getPatientId())
                    .setPatientName(appointment.getPatientName())
                    .setDoctorId(appointment.getDoctorId())
                    .setDoctorName(appointment.getDoctorName())
                    .setSpecialty(appointment.getSpecialty())
                    .setAppointmentDate(appointment.getAppointmentDate().toString())
                    .setStatus(appointment.getStatus().name())
                    .setDescription(appointment.getDescription())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getAppointment(GetAppointmentRequest request, 
                              StreamObserver<AppointmentResponse> responseObserver) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(request.getId());

            AppointmentResponse response = buildAppointmentResponse(appointment);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void listAppointments(ListAppointmentsRequest request, 
                                StreamObserver<AppointmentListResponse> responseObserver) {
        try {
            List<Appointment> appointments = appointmentService.listAllAppointments();

            AppointmentListResponse.Builder responseBuilder = AppointmentListResponse.newBuilder();
            for (Appointment appointment : appointments) {
                responseBuilder.addAppointments(buildAppointmentResponse(appointment));
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getAppointmentsByPatient(GetAppointmentsByPatientRequest request, 
                                        StreamObserver<AppointmentListResponse> responseObserver) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByPatient(request.getPatientId());

            AppointmentListResponse.Builder responseBuilder = AppointmentListResponse.newBuilder();
            for (Appointment appointment : appointments) {
                responseBuilder.addAppointments(buildAppointmentResponse(appointment));
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getAppointmentsByDoctor(GetAppointmentsByDoctorRequest request, 
                                       StreamObserver<AppointmentListResponse> responseObserver) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByDoctor(request.getDoctorId());

            AppointmentListResponse.Builder responseBuilder = AppointmentListResponse.newBuilder();
            for (Appointment appointment : appointments) {
                responseBuilder.addAppointments(buildAppointmentResponse(appointment));
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void cancelAppointment(CancelAppointmentRequest request, 
                                 StreamObserver<AppointmentResponse> responseObserver) {
        try {
            Appointment appointment = appointmentService.cancelAppointment(request.getId(), request.getReason());

            AppointmentResponse response = buildAppointmentResponse(appointment);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private AppointmentResponse buildAppointmentResponse(Appointment appointment) {
        return AppointmentResponse.newBuilder()
                .setId(appointment.getId())
                .setPatientId(appointment.getPatientId())
                .setPatientName(appointment.getPatientName())
                .setDoctorId(appointment.getDoctorId())
                .setDoctorName(appointment.getDoctorName())
                .setSpecialty(appointment.getSpecialty())
                .setAppointmentDate(appointment.getAppointmentDate().toString())
                .setStatus(appointment.getStatus().name())
                .setDescription(appointment.getDescription())
                .build();
    }
}
