package empik.complaints_service.service;

import empik.complaints_service.controller.dto.ComplaintResponse;
import empik.complaints_service.service.country.CountryProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import empik.complaints_service.model.ComplaintEntity;
import empik.complaints_service.repository.ComplaintRepository;

import static java.util.stream.Collectors.toUnmodifiableSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ClockService clockService;
    private final CountryProviderService countryProviderService;
//    private final Executor executor;

    @Override
    public void createComplaint(String complaintant, Long productId, String content, String ip) {
        var existing = complaintRepository.findOneByProductIdAndComplaintant(productId, complaintant);
        if (existing == null) {
            var country = ip != null ? countryProviderService.getCountry(ip) : null;
            complaintRepository.save(buildComplaintEntity(productId, content, complaintant, ip, country));
        } else {
            complaintRepository.incrementCounter(complaintant, productId);
        }
    }

    @Override
    public int updateComplaint(String username, Long complaintId, String content) {
        return complaintRepository.updateComplaint(username, complaintId, content);
    }

    @Override
    public List<ComplaintResponse> findComplaints(String complaintant) {
        var complaints = complaintRepository.findByComplaintant(complaintant);
        // fetch country on read if failed to do so on creation
//        updateCountryIfMissing(complaints);
        return complaints.stream()
                .map(this::convertEntity)
                .sorted(Comparator.comparing(ComplaintResponse::getCreationTimestamp).reversed())
                .toList();
    }

//    /**
//     * In case complaint is missing country (for instance external service was down during creation),
//     * we try to fetch country during complaints read. 5 second timeout is used to prevent slow
//     * external service from slowing down our service.
//     */
//    private void updateCountryIfMissing(List<ComplaintEntity> complaints) {
//        var missingCountryComplaints = complaints.stream()
//                .filter(this::isCountryMissing)
//                .toList();
//        var ips = missingCountryComplaints.stream()
//                .map(ComplaintEntity::getIp)
//                .collect(toUnmodifiableSet());
//        var ipToCountryMap = executor.executeInParallel(ips, countryProviderService::getCountry);
//        var updatedComplaints = missingCountryComplaints.stream()
//                .filter(c -> ipToCountryMap.containsKey(c.getIp()))
//                .map(c -> {
//                    c.setCountry(ipToCountryMap.get(c.getIp()));
//                    return c;
//                }).toList();
//        if (!updatedComplaints.isEmpty()) {
//            complaintRepository.saveAll(updatedComplaints);
//        }
//    }

    private boolean isCountryMissing(ComplaintEntity c) {
        return c.getIp() != null && c.getCountry() == null;
    }

    private ComplaintEntity buildComplaintEntity(Long productId, String content, String complaintant, String ip, String country) {
        return ComplaintEntity.builder()
                .productId(productId)
                .content(content)
                .complaintant(complaintant)
                .creationTimestamp(ZonedDateTime.now(clockService.getClock()))
                .ip(ip)
                .counter(1)
                .country(country)
                .build();
    }

    public ComplaintResponse convertEntity(ComplaintEntity entity) {
        return ComplaintResponse.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .content(entity.getContent())
                .complaintant(entity.getComplaintant())
                .creationTimestamp(entity.getCreationTimestamp())
                .country(entity.getCountry())
                .counter(entity.getCounter())
                .build();
    }
}
