package com.subbu.disaster_management_system.controller;

import com.subbu.disaster_management_system.model.Shelter;
import com.subbu.disaster_management_system.repository.ShelterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shelters")
@CrossOrigin(origins = "*")
public class ShelterController {

    @Autowired
    private ShelterRepository shelterRepository;

    @GetMapping
    public List<Shelter> getAllShelters() {
        return shelterRepository.findAll();
    }

    @PostMapping
    public Shelter createShelter(@RequestBody Shelter shelter) {
        if (shelter.getShelterId() == null) {
            shelter.setShelterId("SH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (shelter.getSafetyRating() == null) {
            shelter.setSafetyRating("A");
        }
        return shelterRepository.save(shelter);
    }

    @PutMapping("/{id}/occupancy")
    public ResponseEntity<Shelter> updateOccupancy(
            @PathVariable Long id, 
            @RequestParam Integer occupied) {
        return shelterRepository.findById(id)
                .map(shelter -> {
                    // Update room configurations if needed, or simply verify structural safety
                    if (shelter.verifyStructuralSafety()) {
                        Shelter updated = shelterRepository.save(shelter);
                        return ResponseEntity.ok(updated);
                    } else {
                        return ResponseEntity.badRequest().<Shelter>build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
