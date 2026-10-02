package edu.cibertec.account.service;

import edu.cibertec.account.dto.StatusResponse;
import edu.cibertec.account.dto.StatusRequest;

import java.util.List;

public interface StatusService {

    List<StatusResponse> findAll();

    StatusResponse findById(Long id);

    StatusResponse create(StatusRequest request);

    StatusResponse update(Long id, StatusRequest request);

    void delete(Long id);
}
