package empik.complaints_service.controller;


import empik.complaints_service.api.ComplaintApi;
import empik.complaints_service.controller.dto.ComplaintResponse;
import empik.complaints_service.controller.dto.CreateComplaintRequest;
import empik.complaints_service.controller.dto.UpdateComplaintRequest;
import empik.complaints_service.service.ComplaintService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ComplaintController implements ComplaintApi {

    private final ComplaintService complaintService;

    @Value("${header.ip:X-Forwarded-For}")
    private String ipHeader;

    @Override
    public void createComplaint(HttpServletRequest httpServletRequest, CreateComplaintRequest createComplaintRequest, Authentication authentication) {
        complaintService.createComplaint(((User) authentication.getPrincipal()).getUsername(),
                createComplaintRequest.getProductId(),
                createComplaintRequest.getContent(),
                httpServletRequest.getHeader(ipHeader)
        );
    }

    @Override
    public List<ComplaintResponse> findComplaints(Authentication authentication) {
        return complaintService.findComplaints(((User) authentication.getPrincipal()).getUsername());
    }

    @Override
    public ResponseEntity<Void> updateComplaint(Long complaintId, UpdateComplaintRequest updateComplaintRequest, Authentication authentication) {
        var result = complaintService.updateComplaint(((User) authentication.getPrincipal()).getUsername(),
                complaintId,
                updateComplaintRequest.getContent()
        );
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
