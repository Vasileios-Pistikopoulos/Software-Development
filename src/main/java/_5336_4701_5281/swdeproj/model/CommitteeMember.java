package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "committee_members")
public class CommitteeMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ο χρήστης που έχει login
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private CommitteeProfile profile;

    // μελλοντικά: traineeships υπό παρακολούθηση
    // @OneToMany(mappedBy = "committee")
    // private List<TraineeshipPosition> monitoredTraineeships;
}
