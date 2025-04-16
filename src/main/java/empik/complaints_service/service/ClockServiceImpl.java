package empik.complaints_service.service;

import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class ClockServiceImpl implements ClockService {

    @Override
    public Clock getClock() {
        return Clock.systemDefaultZone();
    }
}
