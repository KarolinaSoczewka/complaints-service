package empik.complaints_service.repository;

import empik.complaints_service.model.ComplaintEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ComplaintRepository extends CrudRepository<ComplaintEntity, Long> {

    List<ComplaintEntity> findByComplaintant(String complaintant);

    ComplaintEntity findOneByProductIdAndComplaintant(Long productId, String complaintant);

    @Modifying
    @Query("UPDATE complaint c set c.counter = c.counter + 1 WHERE c.productId = :productId and c.complaintant = :complaintant")
    @Transactional
    void incrementCounter(String complaintant, Long productId);

    @Modifying
    @Query("UPDATE complaint c set c.content = :content WHERE c.id = :complaintId and c.complaintant = :complaintant")
    @Transactional
    int updateComplaint(String complaintant, Long complaintId, String content);
}
