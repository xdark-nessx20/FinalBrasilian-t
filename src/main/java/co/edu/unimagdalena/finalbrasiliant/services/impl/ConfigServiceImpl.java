package co.edu.unimagdalena.finalbrasiliant.services.impl;

import co.edu.unimagdalena.finalbrasiliant.api.dto.ConfigDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.ConfigRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.ConfigService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.ConfigMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    private final ConfigRepository configRepo;
    private final ConfigMapper mapper;

    private final Map<String, BigDecimal> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initCache(){
        cache.clear();
        configRepo.findAll().forEach(config -> cache.put(config.getKey(), config.getValue()));
    }

    @Override
    public ConfigResponse create(ConfigCreateRequest request) {
        var created = configRepo.save(mapper.toEntity(request));
        cache.put(created.getKey(), created.getValue());
        return mapper.toResponse(created);
    }

    @Override
    public ConfigResponse update(String key, ConfigUpdateRequest request) {
        var config = configRepo.findByKeyIgnoreCase(key).orElseThrow(
                () -> new NotFoundException("Config '%s' not found.".formatted(key))
        );
        mapper.patch(config, request);
        var saved = configRepo.save(config);
        cache.put(saved.getKey(), saved.getValue());
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(String key) {
        configRepo.delete(key);
        if (!configRepo.existsByKeyIgnoreCase(key)) cache.remove(key);
    }

    @Override
    public List<ConfigResponse> getAll() {
        return configRepo.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigResponse get(String key) {
        return configRepo.findByKeyIgnoreCase(key).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Config '%s' not found.".formatted(key)));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getValue(String key) {
        return cache.computeIfAbsent(key, k -> {
            var config = configRepo.findByKeyIgnoreCase(k).orElseThrow(
                    () -> new NotFoundException("Config '%s' not found.".formatted(key))
            );
            return config.getValue();
        });
    }
}
