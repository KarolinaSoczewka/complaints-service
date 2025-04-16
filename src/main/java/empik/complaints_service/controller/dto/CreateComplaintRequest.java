package empik.complaints_service.controller.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
public class CreateComplaintRequest {
    @Size(max = 1024)
    private String content;
    private Long productId;
}
