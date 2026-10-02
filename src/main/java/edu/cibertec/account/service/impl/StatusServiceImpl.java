package edu.cibertec.account.service.impl;

import edu.cibertec.account.domain.entity.Status;
import edu.cibertec.account.dto.StatusResponse;
import edu.cibertec.account.dto.StatusRequest;
import edu.cibertec.account.exception.DuplicateResourceException;
import edu.cibertec.account.exception.ResourceNotFoundException;
import edu.cibertec.account.mapper.StatusMapper;
import edu.cibertec.account.repository.StatusRepository;
import edu.cibertec.account.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;
    private final StatusMapper statusMapper;

    @Override
    public List<StatusResponse> findAll() {
        return statusRepository.findAll().stream()
                .map(statusMapper::toResponse)
                .toList();
    }

    @Override
    public StatusResponse findById(Long id) {
        return statusMapper.toResponse(findStatus(id));
    }

    @Override
    @Transactional
    public StatusResponse create(StatusRequest request) {
        ensureUnique(request.name(), null);
        Status saved = statusRepository.save(statusMapper.toEntity(request));
        return statusMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public StatusResponse update(Long id, StatusRequest request) {
        Status status = findStatus(id);
        ensureUnique(request.name(), id);
        statusMapper.updateEntity(status, request);
        return statusMapper.toResponse(statusRepository.save(status));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        statusRepository.delete(findStatus(id));
    }

    private Status findStatus(Long id) {
        return statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
    }

    private void ensureUnique(String name, Long idStatus) {
        boolean exists = idStatus == null
                ? statusRepository.existsByName(name)
                : statusRepository.existsByNameAndIdStatusNot(name, idStatus);
        if (exists) {
            throw new DuplicateResourceException("Status name already exists");
        }
    }
}
