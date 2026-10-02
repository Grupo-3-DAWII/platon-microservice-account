package edu.cibertec.account.controller;

import edu.cibertec.account.dto.StatusResponse;
import edu.cibertec.account.dto.StatusRequest;
import edu.cibertec.account.service.StatusService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statuses")
@RequiredArgsConstructor
@Tag(name = "Statuses", description = "Status catalog management")
@SecurityRequirement(name = "basicAuth")
public class StatusController {

    private final StatusService statusService;

    @GetMapping
    public List<StatusResponse> findAll() {
        return statusService.findAll();
    }

    @GetMapping("/{id}")
    public StatusResponse findById(@PathVariable Long id) {
        return statusService.findById(id);
    }

    @PostMapping
    public ResponseEntity<StatusResponse> create(@Valid @RequestBody StatusRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(statusService.create(request));
    }

    @PutMapping("/{id}")
    public StatusResponse update(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return statusService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        statusService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
