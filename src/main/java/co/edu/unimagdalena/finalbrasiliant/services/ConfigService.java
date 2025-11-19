package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.ConfigDTOs.*;

import java.math.BigDecimal;
import java.util.List;

public interface ConfigService {
    ConfigResponse create(ConfigCreateRequest request);
    ConfigResponse get(String key);
    ConfigResponse update(String key, ConfigUpdateRequest request);
    void delete(String key);
    List<ConfigResponse> getAll();
    BigDecimal getValue(String key);
}
