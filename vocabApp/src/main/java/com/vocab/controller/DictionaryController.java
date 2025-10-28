package com.vocab.controller;

import com.vocab.model.Dictionary;
import com.vocab.repository.DictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/dictionary")
@CrossOrigin(origins = "*")
public class DictionaryController {
    
    @Autowired
    private DictionaryRepository dictionaryRepository;
    
    @GetMapping("/random")
    public ResponseEntity<?> getRandomWord() {
        try {
            System.out.println(" Fetching random word from dictionary");
            
            List<Dictionary> allWords = dictionaryRepository.findAll();
            if (allWords.isEmpty()) {
                System.out.println("⚠️ No words in dictionary");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No words in dictionary"));
            }
            
            Random random = new Random();
            Dictionary randomWord = allWords.get(random.nextInt(allWords.size()));
            
            System.out.println(" Random word: " + randomWord.getWord());
            
            return ResponseEntity.ok(randomWord);
        } catch (Exception e) {
            System.err.println(" Error fetching random word: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch random word", "details", e.getMessage()));
        }
    }
    
    @GetMapping("/word/{word}")
    public ResponseEntity<?> getWordDefinition(@PathVariable("word") String word) {
        try {
            System.out.println(" Looking up word: " + word);
            
            Dictionary result = dictionaryRepository.findByWord(word.toLowerCase());
            if (result == null) {
                System.out.println("⚠️ Word not found: " + word);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Word not found"));
            }
            
            System.out.println(" Found definition for: " + word);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println(" Error fetching word: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch word", "details", e.getMessage()));
        }
    }
    
    @PostMapping("/add")
    public ResponseEntity<?> addWord(@RequestBody Dictionary word) {
        try {
            System.out.println(" Adding word to dictionary: " + word.getWord());
            
            word.setWord(word.getWord().toLowerCase());
            Dictionary saved = dictionaryRepository.save(word);
            
            System.out.println(" Word added with ID: " + saved.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            System.err.println(" Error adding word: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add word", "details", e.getMessage()));
        }
    }
}