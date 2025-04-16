package empik.complaints_service.controller.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class ComplaintResponse {
    private long id;
    private long productId;
    private String content;
    private String complaintant;
    private ZonedDateTime creationTimestamp;
    //  "ISO3166-1 alpha-2 two-letter country code
    private String country;
    private int counter;
}
