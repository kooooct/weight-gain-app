package org.example.weightgainapp.controller;

import org.example.weightgainapp.dto.BmrRequest;
import org.example.weightgainapp.dto.BmrResponse;
import org.example.weightgainapp.service.BmrService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
public class BmrController {
    private final BmrService bmrService;

    @PostMapping("/calculate")
    public BmrResponse calculate(@RequestBody BmrRequest bmrRequest) {
        return bmrService.calculate(bmrRequest);
    }
}
