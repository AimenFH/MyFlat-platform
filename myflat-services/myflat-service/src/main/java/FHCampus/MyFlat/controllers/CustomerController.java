package FHCampus.MyFlat.controllers;

import FHCampus.MyFlat.dtos.BookApartmentDto;
import FHCampus.MyFlat.dtos.ApartmentDto;
import FHCampus.MyFlat.dtos.SearchApartmentDto;
import FHCampus.MyFlat.services.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/v1/apartments")
    public ResponseEntity<List<ApartmentDto>> getAllApartments() {
        List<ApartmentDto> apartmentDtoList = customerService.getAllApartments();
        return ResponseEntity.ok(apartmentDtoList);
    }

    @GetMapping("/v1/apartment/{apartmentId}")
    public ResponseEntity<ApartmentDto> getApartmentById(@PathVariable Long apartmentId) {
        ApartmentDto apartmentDto = customerService.getApartmentById(apartmentId);
        if (apartmentDto != null) return ResponseEntity.ok(apartmentDto);
        return ResponseEntity.notFound().build();
    }


    @GetMapping("/v1/apartment/bookings/{userId}")
    public ResponseEntity<?> getBookingsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(customerService.getBookingsByUserId(userId));
    }

}
