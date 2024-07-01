package fhcampus.myflat.controllers;

import fhcampus.myflat.dtos.DistributionRequestDto;
import fhcampus.myflat.dtos.DocumentDto;
import fhcampus.myflat.exceptions.NoNotificationsFoundException;
import fhcampus.myflat.entities.Apartment;
import fhcampus.myflat.entities.Document;
import fhcampus.myflat.entities.Notifications;
import fhcampus.myflat.entities.User;
import fhcampus.myflat.repositories.ApartmentRepository;
import fhcampus.myflat.repositories.DocumentRepository;
import fhcampus.myflat.repositories.KeyManagementRepository;
import fhcampus.myflat.repositories.UserRepository;
import fhcampus.myflat.services.propertymanagement.PropertyManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/property-management")
public class PropertyManagementCommunicationController {

    private final PropertyManagementService propertyManagementService;
    private final DocumentRepository documentRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final KeyManagementRepository keyManagementRepository;

    //////////////////////////// distribute notification
    @PostMapping("/v1/distribute")
    public ResponseEntity<?> distributeNotification(@RequestBody DistributionRequestDto distributionRequestDto) {
        boolean success = propertyManagementService.distributeNotification(distributionRequestDto);
        if (success)
            return ResponseEntity.status(HttpStatus.CREATED).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("v1/notifications")
    public ResponseEntity<?> getNotifications(@RequestParam(required = false) Integer buildingId, @RequestParam(required = false) Integer topId) {
        try {
            List<Notifications> notifications = propertyManagementService.getNotifications(buildingId, topId);
            return ResponseEntity.ok(notifications);
        } catch (NoNotificationsFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /////////////////////Document
    @PostMapping(value = "/v1/document")
    public ResponseEntity<Object> createDocument(
            @RequestPart("documentDto") DocumentDto documentDto,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            Optional<Apartment> apartment = apartmentRepository.findById(documentDto.getApartmentId());
            Optional<User> user = userRepository.findById(documentDto.getUserId());

            if (apartment.isPresent() && user.isPresent()) {
                Document document = new Document();
                document.setApartment(apartment.get());
                document.setTitle(documentDto.getTitle());
                document.setContent(file.getBytes());
                document.setArchived(documentDto.isArchived());
                document.setUser(user.get());
                Document savedDocument = documentRepository.save(document);
                return new ResponseEntity<>(savedDocument.documentDto(), HttpStatus.CREATED);
            } else if (apartment.isEmpty()) {
                return new ResponseEntity<>("Apartment not found", HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            return new ResponseEntity<>("Error occurred while decoding file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/v1/document/{apartmentId}/{documentId}")
    public ResponseEntity<Object> updateDocumentState(@PathVariable Long apartmentId, @PathVariable Long documentId, @RequestBody DocumentDto documentDto) {
        try {
            byte[] fileContent = Base64.getDecoder().decode(documentDto.getContent());
            List<Document> documents = documentRepository.findAll();
            Optional<Document> apartmentDocument = documents.stream()
                    .filter(document -> document.getApartment().getId().equals(apartmentId) && document.getId().equals(documentId))
                    .findFirst();

            if (apartmentDocument.isPresent()) {
                Document document = apartmentDocument.get();
                document.setArchived(documentDto.isArchived());
                document.setContent(fileContent);
                Document updatedDocument = documentRepository.save(document);
                return new ResponseEntity<>(updatedDocument.documentDto(), HttpStatus.OK);
            }
            return new ResponseEntity<>("Document not found for the given apartment", HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Error occurred while decoding file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/v1/document")
    public ResponseEntity<Object> getAllDocuments() {
        List<Document> documents = documentRepository.findAll();
        return new ResponseEntity<>(documents.stream().map(Document::documentDto).toList(), HttpStatus.OK);
    }

    @DeleteMapping("/v1/document")
    public ResponseEntity<Void> deleteAllDocuments() {
        documentRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/v1/document/{documentId}")
    public ResponseEntity<?> deleteDocumentById(@PathVariable Long documentId) {
        Optional<Document> document = documentRepository.findById(documentId);
        if (document.isPresent()) {
            documentRepository.deleteById(documentId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document not found");
        }
    }
}
