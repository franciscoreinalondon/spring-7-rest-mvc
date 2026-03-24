package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.services.MilkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiPaths.MILKS)
public class MilkController {

    private final MilkService milkService;

    @PostMapping
    public ResponseEntity<MilkResponse> create(@Valid @RequestBody MilkRequest request) {
        log.info("Creating milk with name and upc: {}, {}", request.name(), request.upc());
        var milkResponse = milkService.create(request);
        var location = URI.create(ApiPaths.MILKS + "/" + milkResponse.id());

        return ResponseEntity.created(location).body(milkResponse);
    }

    @GetMapping(ApiPaths.MILK_ID)
    public MilkResponse getById(@PathVariable("milkId") UUID milkId) {
        log.info("Getting milk by id: {}", milkId);

        return milkService.getById(milkId);
    }

    @GetMapping
    public Page<MilkResponse> list(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "milkType", required = false) MilkType milkType,
            Pageable pageable) {
        log.info("Getting all milks");

        return milkService.list(name, milkType, pageable);
    }

    @PutMapping(ApiPaths.MILK_ID)
    public MilkResponse update(@PathVariable("milkId") UUID milkId, @Valid @RequestBody MilkRequest request) {
        log.info("Updating milk with id: {}", milkId);

        return milkService.update(milkId, request);
    }

    @PatchMapping(ApiPaths.MILK_ID)
    public MilkResponse patch(@PathVariable("milkId") UUID milkId, @Valid @RequestBody MilkPatchRequest request) {
        log.info("Patching milk with id: {}", milkId);

        return milkService.patch(milkId, request);
    }

    @DeleteMapping(ApiPaths.MILK_ID)
    public ResponseEntity<Void> delete(@PathVariable("milkId") UUID milkId) {
        log.info("Deleting customer with id: {}", milkId);
        milkService.delete(milkId);

        return ResponseEntity.noContent().build();
    }
}
