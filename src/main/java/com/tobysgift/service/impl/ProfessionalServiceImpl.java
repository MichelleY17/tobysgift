package com.tobysgift.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tobysgift.model.Professional;
import com.tobysgift.model.ProfessionalType;
import com.tobysgift.repository.ProfessionalRepository;
import com.tobysgift.repository.ProfessionalTypeRepository;
import com.tobysgift.service.ProfessionalService;

@Service
public class ProfessionalServiceImpl implements ProfessionalService {
    
    private final ProfessionalRepository professionalRepository;
    private final ProfessionalTypeRepository professionalTypeRepository;
    
    @Value("${app.upload.dir.professionals:uploads/professionals}")
    private String uploadDir;
    
    @Autowired
    public ProfessionalServiceImpl(
        ProfessionalRepository professionalRepository, 
        ProfessionalTypeRepository professionalTypeRepository
    ) {
        this.professionalRepository = professionalRepository;
        this.professionalTypeRepository = professionalTypeRepository;
    }
    
    @Override
    public List<Professional> findAll() {
        return professionalRepository.findAll();
    }
    
    @Override
    public Page<Professional> findAll(Pageable pageable) {
        return professionalRepository.findAll(pageable);
    }
    
    @Override
    public Optional<Professional> findById(Long id) {
        return professionalRepository.findById(id);
    }
    
    @Override
    public Page<Professional> findByType(Long typeId, Pageable pageable) {
        Optional<ProfessionalType> type = professionalTypeRepository.findById(typeId);
        return type.map(value -> professionalRepository.findByTipo(value, pageable))
                   .orElseGet(() -> Page.empty(pageable));
    }
    
    @Override
    public Page<Professional> searchProfessionals(String keyword, Pageable pageable) {
        return professionalRepository.searchByKeyword(keyword, pageable);
    }
    
    @Override
    public Page<Professional> searchProfessionalsByType(String keyword, Long typeId, Pageable pageable) {
        Optional<ProfessionalType> type = professionalTypeRepository.findById(typeId);
        
        if (type.isPresent()) {
            return professionalRepository.searchByKeywordAndTipo(keyword, type.get(), pageable);
        }
        
        return Page.empty(pageable);
    }
    
    @Override
    @Transactional
    public Professional save(Professional professional, MultipartFile imageFile) {
        // Salva l'immagine se presente
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageName = saveImage(imageFile);
            professional.setImmagine(imageName);
        }
        
        return professionalRepository.save(professional);
    }
    
    @Override
    @Transactional
    public Professional update(Professional professional, MultipartFile imageFile) {
        // Recupera il professionista esistente
        Professional existingProfessional = professionalRepository.findById(professional.getId())
                .orElseThrow(() -> new RuntimeException("Professionista non trovato con ID: " + professional.getId()));
        
        // Aggiorna i campi
        existingProfessional.setNome(professional.getNome());
        existingProfessional.setCognome(professional.getCognome());
        existingProfessional.setEmail(professional.getEmail());
        existingProfessional.setTelefono(professional.getTelefono());
        existingProfessional.setSpecializzazione(professional.getSpecializzazione());
        existingProfessional.setDescrizione(professional.getDescrizione());
        existingProfessional.setTipo(professional.getTipo());
        
        // Aggiorna l'immagine solo se ne è stata caricata una nuova
        if (imageFile != null && !imageFile.isEmpty()) {
            // Elimina la vecchia immagine se presente
            if (existingProfessional.getImmagine() != null) {
                deleteImage(existingProfessional.getImmagine());
            }
            
            // Salva la nuova immagine
            String imageName = saveImage(imageFile);
            existingProfessional.setImmagine(imageName);
        }
        
        return professionalRepository.save(existingProfessional);
    }
    
    @Override
    @Transactional
    public boolean delete(Long id) {
        Optional<Professional> professional = professionalRepository.findById(id);
        
        if (professional.isPresent()) {
            // Elimina l'immagine se presente
            if (professional.get().getImmagine() != null) {
                deleteImage(professional.get().getImmagine());
            }
            
            professionalRepository.deleteById(id);
            return true;
        }
        
        return false;
    }
    
    @Override
    public List<Professional> findMostBookedProfessionals(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return professionalRepository.findMostBookedProfessionals(pageable);
    }
    
    @Override
    public Page<Professional> findBySpecialization(String specializzazione, Pageable pageable) {
        return professionalRepository.findBySpecializzazioneContainingIgnoreCase(specializzazione, pageable);
    }
    
    @Override
    public long countProfessionals() {
        return professionalRepository.count();
    }
    
    @Override
    public List<ProfessionalType> findAllProfessionalTypes() {
        return professionalTypeRepository.findAll();
    }
    
    @Override
    public Optional<ProfessionalType> findProfessionalTypeById(Long id) {
        return professionalTypeRepository.findById(id);
    }
    
    @Override
    public Optional<ProfessionalType> findProfessionalTypeByName(String name) {
        return professionalTypeRepository.findByNome(name);
    }
    
    @Override
    @Transactional
    public ProfessionalType saveProfessionalType(ProfessionalType professionalType) {
        return professionalTypeRepository.save(professionalType);
    }
    
    @Override
    @Transactional
    public boolean deleteProfessionalType(Long id) {
        Optional<ProfessionalType> type = professionalTypeRepository.findById(id);
        
        if (type.isPresent()) {
            // Verifica che non ci siano professionisti associati
            if (!type.get().getProfessionals().isEmpty()) {
                throw new RuntimeException("Impossibile eliminare il tipo di professionista perché ha professionisti associati");
            }
            
            professionalTypeRepository.deleteById(id);
            return true;
        }
        
        return false;
    }
    
    /**
     * per salvare un'immagine nel filesystem e restituisce il nome del file
     * 
     * @param imageFile file dell'immagine
     * @return restituisce il nome del file salvato
     */
    private String saveImage(MultipartFile imageFile) {
        try {
            // Crea la directory se non esiste
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Directory creata on-demand: " + uploadPath.toAbsolutePath());
            }
            
            // Log di debugging
            System.out.println("Tentativo di salvare immagine. Nome originale: " + imageFile.getOriginalFilename());
            System.out.println("Directory di destinazione: " + uploadPath.toAbsolutePath());
            
            // Per generare un nome univoco per il file
            String originalFilename = imageFile.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID() + extension;
            
            // Per salvare il file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("Immagine salvata con successo: " + filePath.toAbsolutePath());
            return filename;
        } catch (IOException e) {
            System.err.println("Errore nel salvare l'immagine: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Errore nel salvare l'immagine: " + e.getMessage());
        }
    }
    
    /**
     * per eliminare un'immagine dal filesystem
     * 
     * @param imageName nome del file da eliminare
     */
    private void deleteImage(String imageName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(imageName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log dell'errore, ma non interrompe l'operazione
            System.err.println("Errore nell'eliminare l'immagine: " + e.getMessage());
        }
    }
}
