package empik.complaints_service.service;

import java.util.List;
import empik.complaints_service.controller.dto.ComplaintResponse;

public interface ComplaintService {

    void createComplaint(String complaintant, Long productId, String content, String ip);

    int updateComplaint(String username, Long complaintId, String content);

    List<ComplaintResponse> findComplaints(String complaintant);
}
