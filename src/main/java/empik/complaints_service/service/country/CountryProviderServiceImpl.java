package empik.complaints_service.service.country;

import empik.complaints_service.service.country.dto.IpApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Locale;

import static java.util.Locale.IsoCountryCode.PART1_ALPHA2;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@RequiredArgsConstructor
@Slf4j
public class CountryProviderServiceImpl implements CountryProviderService {

    private final RestClient restClient = restClient(5000);

    @Override
    public String getCountry(String ip) {
        try {
            var country = restClient.get()
                    .uri("http://ip-api.com/json/{ip}", ip)
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .body(IpApiResponse.class)
                    .getCountryCode();
            if (country == null) return null;
            return Locale.getISOCountries(PART1_ALPHA2).contains(country.toUpperCase()) ?
                    country.toUpperCase() : null;
        } catch (Exception e) {
            log.error("Unable to fetch country", e);
            return null;
        }
    }

    private static RestClient restClient(int readTimeout) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(readTimeout);
        return RestClient
                .builder()
                .requestFactory(factory)
                .build();
    }
}
