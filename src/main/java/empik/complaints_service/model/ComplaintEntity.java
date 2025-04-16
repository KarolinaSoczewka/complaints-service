package empik.complaints_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.ZonedDateTime;

@Entity(name = "complaint")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ComplaintEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private String content;
    private ZonedDateTime creationTimestamp;
    private String complaintant;
    private String ip;
    private String country;
    private Integer counter;
}
