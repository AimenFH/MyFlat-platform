package FHCampus.MyFlat.controllers;

import FHCampus.MyFlat.dtos.*;
import FHCampus.MyFlat.repositories.UserRepository;
import FHCampus.MyFlat.services.admin.AdminService;
import FHCampus.MyFlat.services.auth.AuthService;
import FHCampus.MyFlat.services.customer.CustomerService;
import FHCampus.MyFlat.services.jwt.UserService;
import FHCampus.MyFlat.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    private final AuthService authService;

    // sign up
    @PostMapping("/v1/signup")
    public ResponseEntity<?> createCustomer(@RequestBody SignupRequest signupRequest) {
        if (authService.hasCustomerWithEmail(signupRequest.getEmail()))
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Email already exist. Try again with another email");
        UserDto createdUserDto = authService.createCustomer(signupRequest);
        if (createdUserDto == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad Request!");
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUserDto);    }

// admin section
    @PostMapping("/v1/apartment")
    public ResponseEntity<?> postCar(@ModelAttribute ApartmentDto apartmentDto) {
        boolean success = adminService.postCar(apartmentDto);
        if (success)
            return ResponseEntity.status(HttpStatus.CREATED).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/v1/apartments")
    public ResponseEntity<List<ApartmentDto>> getAllCars() {
        List<ApartmentDto> apartmentDtoList = adminService.getAllApartments();
        return ResponseEntity.ok(apartmentDtoList);
    }

    @DeleteMapping("/v1/apartment/{apartmentId}")
    public ResponseEntity<Void> deleteApartment(@PathVariable Long apartmentId) {
        adminService.deleteApartment(apartmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/v1/apartment/{apartmentId}")
    public ResponseEntity<ApartmentDto> getApartmentById(@PathVariable Long apartmentId) {
        ApartmentDto apartmentDto = adminService.getApartmentById(apartmentId);
        if (apartmentDto != null) return ResponseEntity.ok(apartmentDto);
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/v1/apartment/{apartmentId}")
    public ResponseEntity<?> updateApartment(@PathVariable Long apartmentId, @ModelAttribute ApartmentDto apartmentDto) throws IOException {
        try {
            boolean success = adminService.updateApartment(apartmentId, apartmentDto);
            if (success) return ResponseEntity.ok().build();
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }

    @GetMapping("/v1/apartment/bookings")
    public ResponseEntity<?> getBookings() {
        return ResponseEntity.ok(adminService.getBookings());
    }

    @GetMapping("/v1/apartment/booking/{bookingId}/{status}")
    public ResponseEntity<?> changeBookingStatus(@PathVariable Long bookingId, @PathVariable String status) {
        boolean success = adminService.changeBookingStatus(bookingId, status);
        if (success) return ResponseEntity.ok().build();
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/v1/apartment/search")
    public ResponseEntity<?> searchApartment(@RequestBody SearchApartmentDto searchApartmentDto) {
        return ResponseEntity.ok(adminService.searchApartment(searchApartmentDto));
    }

    // customer service

    private final CustomerService customerService;

    @PostMapping("/v1/apartment/book/{apartmentId}")
    public ResponseEntity<?> bookApartment(@PathVariable Long apartmentId, @RequestBody BookApartmentDto bookApartmentDto) {
        boolean success = customerService.bookApartment(apartmentId, bookApartmentDto);
        if (success) return ResponseEntity.ok().build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }


}
