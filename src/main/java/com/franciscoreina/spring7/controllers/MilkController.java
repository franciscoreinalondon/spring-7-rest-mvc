package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.dtos.milk.MilkCreateRequest;
import com.franciscoreina.spring7.dtos.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dtos.milk.MilkResponse;
import com.franciscoreina.spring7.dtos.milk.MilkUpdateRequest;
import com.franciscoreina.spring7.services.MilkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(ApiPaths.MILKS)
public class MilkController {

    private final MilkService milkService;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody MilkCreateRequest request) {
        MilkResponse milkResponse = milkService.create(request);

        URI location = URI.create(ApiPaths.MILKS + "/" + milkResponse.id());
        return ResponseEntity.created(location).build();
    }

    @GetMapping(ApiPaths.MILK_ID)
    public MilkResponse getById(@PathVariable("milkId") UUID milkId) {
        return milkService.getById(milkId);
    }

    @GetMapping
    public List<MilkResponse> list() {
        return milkService.list();
    }

    @PutMapping(ApiPaths.MILK_ID)
    public ResponseEntity<Void> update(@PathVariable("milkId") UUID milkId, @Valid @RequestBody MilkUpdateRequest request) {
        milkService.update(milkId, request);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping(ApiPaths.MILK_ID)
    public ResponseEntity<Void> patch(@PathVariable("milkId") UUID milkId, @Valid @RequestBody MilkPatchRequest request) {
        milkService.patch(milkId, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(ApiPaths.MILK_ID)
    public ResponseEntity<Void> delete(@PathVariable("milkId") UUID milkId) {
        milkService.delete(milkId);

        return ResponseEntity.noContent().build();
    }
}
