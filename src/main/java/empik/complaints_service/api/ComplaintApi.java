package empik.complaints_service.api;

import empik.complaints_service.controller.dto.ComplaintResponse;
import empik.complaints_service.controller.dto.CreateComplaintRequest;

import empik.complaints_service.controller.dto.UpdateComplaintRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ComplaintApi {

    @PostMapping("/complaint")
    void createComplaint(HttpServletRequest httpServletRequest,
                         @Valid @RequestBody CreateComplaintRequest createComplaintRequest,
                         Authentication authentication);

    @GetMapping("/complaint")
    List<ComplaintResponse> findComplaints(Authentication authentication);

    @PutMapping("/complaint/{complaintId}")
    ResponseEntity<Void> updateComplaint(@PathVariable Long complaintId,
                                         @Valid @RequestBody UpdateComplaintRequest updateComplaintRequest,
                                         Authentication authentication);
}
