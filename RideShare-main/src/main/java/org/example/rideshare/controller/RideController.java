package org.example.rideshare.controller;

import org.example.rideshare.dto.CreateRideRequest;
import org.example.rideshare.dto.RideResponse;
import org.example.rideshare.model.Ride;
import org.example.rideshare.service.RideService;
import org.example.rideshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class RideController {

    @Autowired
    private RideService rideService;

    @Autowired
    private UserService userService;

    @PostMapping("/rides")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request, Authentication authentication) {
        String username = authentication.getName();
        String userId = userService.getUserIdByUsername(username);
        Ride ride = rideService.createRide(userId, request.getPickupLocation(), request.getDropLocation());
        RideResponse response = new RideResponse(ride.getId(), ride.getUserId(), ride.getDriverId(), ride.getPickupLocation(), ride.getDropLocation(), ride.getStatus(), ride.getCreatedAt());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/driver/rides/requests")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<RideResponse>> getPendingRides() {
        List<Ride> rides = rideService.getPendingRides();
        List<RideResponse> responses = rides.stream()
                .map(ride -> new RideResponse(ride.getId(), ride.getUserId(), ride.getDriverId(), ride.getPickupLocation(), ride.getDropLocation(), ride.getStatus(), ride.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/driver/rides/{rideId}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideResponse> acceptRide(@PathVariable String rideId, Authentication authentication) {
        String username = authentication.getName();
        String driverId = userService.getUserIdByUsername(username);
        Ride ride = rideService.acceptRide(rideId, driverId);
        RideResponse response = new RideResponse(ride.getId(), ride.getUserId(), ride.getDriverId(), ride.getPickupLocation(), ride.getDropLocation(), ride.getStatus(), ride.getCreatedAt());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rides/{rideId}/complete")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<RideResponse> completeRide(@PathVariable String rideId) {
        Ride ride = rideService.completeRide(rideId);
        RideResponse response = new RideResponse(ride.getId(), ride.getUserId(), ride.getDriverId(), ride.getPickupLocation(), ride.getDropLocation(), ride.getStatus(), ride.getCreatedAt());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/rides")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<RideResponse>> getUserRides(Authentication authentication) {
        String username = authentication.getName();
        String userId = userService.getUserIdByUsername(username);
        List<Ride> rides = rideService.getUserRides(userId);
        List<RideResponse> responses = rides.stream()
                .map(ride -> new RideResponse(ride.getId(), ride.getUserId(), ride.getDriverId(), ride.getPickupLocation(), ride.getDropLocation(), ride.getStatus(), ride.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
