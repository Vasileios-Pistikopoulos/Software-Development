package _5336_4701_5281.swdeproj.service;

import _5336_4701_5281.swdeproj.model.ProfessorProfile;
import _5336_4701_5281.swdeproj.model.Traineeship;
import _5336_4701_5281.swdeproj.repository.ProfessorProfileRepository;
import _5336_4701_5281.swdeproj.repository.TraineeshipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class TraineeshipServiceTest {
    @InjectMocks
    private TraineeshipService traineeshipService;

    @Mock
    private TraineeshipRepository traineeshipRepository;

    @Mock
    private ProfessorProfileRepository professorRepository;

    @Test
    void assignSupervisorSuccessfully() {
        // Arrange
        Long traineeshipId = 1L;
        Long professorId = 1L;
        
        Traineeship traineeship = new Traineeship();
        traineeship.setId(traineeshipId);
        
        ProfessorProfile professor = new ProfessorProfile();
        
        when(traineeshipRepository.findById(traineeshipId)).thenReturn(java.util.Optional.of(traineeship));
        when(professorRepository.findById(professorId)).thenReturn(java.util.Optional.of(professor));
        when(traineeshipRepository.save(any(Traineeship.class))).thenReturn(traineeship);

        // Act
        traineeshipService.assignSupervisor(traineeshipId, professorId);

        // Assert
        verify(traineeshipRepository).findById(traineeshipId);
        verify(professorRepository).findById(professorId);
        verify(traineeshipRepository).save(traineeship);
        assertEquals(professor, traineeship.getSupervisor());
    }

    @Test
    void assignSupervisorFailsWhenSupervisorNotFound() {
        // Add logic to test failure when the supervisor does not exist
    }

    @Test
    void getTraineeshipByIdReturnsTraineeship() {
        // Add logic to test retrieval of a traineeship by valid ID
    }

    @Test
    void getTraineeshipByIdThrowsExceptionForInvalidId() {
        // Add logic to test exception when ID is invalid or not found
    }
}