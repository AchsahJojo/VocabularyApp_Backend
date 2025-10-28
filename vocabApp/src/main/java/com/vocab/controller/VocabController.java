package com.vocab.controller;

import com.vocab.model.VocabList;
import com.vocab.model.WordInList;
import com.vocab.repository.VocabListRepository;
import com.vocab.repository.WordInListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vocab")
@CrossOrigin(origins = "*")
public class VocabController {
    @Autowired
    private VocabListRepository vocabListRepository;
    @Autowired
    private WordInListRepository wordInListRepository;

    @GetMapping("/lists/{userId}")
    public ResponseEntity<?> getAllLists(@PathVariable("userId") String userId) {
        try {
            List<VocabList> lists = vocabListRepository.findByUserId(userId);
            return ResponseEntity.ok(lists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch lists"));
        }
    }

    @GetMapping("/lists/{userId}/exclude-history")
    public ResponseEntity<?> getListsExcludingHistory(@PathVariable("userId") String userId) {
        try {
            Optional<VocabList> historyList = vocabListRepository.findFirstByUserIdOrderByCreatedAtAsc(userId);
            List<VocabList> allLists = vocabListRepository.findByUserId(userId);
            if (historyList.isPresent()) {
                allLists.removeIf(list -> list.getId().equals(historyList.get().getId()));
            }
            Map<String, Object> response = new HashMap<>();
            response.put("lists", allLists);
            response.put("vocabHistoryId", historyList.map(VocabList::getId).orElse(null));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch lists"));
        }
    }

    @PostMapping("/lists")
    public ResponseEntity<?> createList(@RequestBody VocabList vocabList) {
        try {
            if (vocabList.getListName() == null || vocabList.getListName().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please enter a list name"));
            }
            VocabList savedList = vocabListRepository.save(vocabList);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "List created successfully");
            response.put("list", savedList);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create list"));
        }
    }

    // this is to get all words in a specific list
    @GetMapping("/words/{userId}/{listId}")
    public ResponseEntity<?> getWordsInList(@PathVariable("userId") String userId,
            @PathVariable("listId") String listId) {
        try {
            Optional<VocabList> list = vocabListRepository.findById(listId);
            List<WordInList> words = wordInListRepository.findByUserIdAndListId(userId, listId);
            Map<String, Object> response = new HashMap<>();
            response.put("listName", list.map(VocabList::getListName).orElse(null));
            response.put("words", words);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch words"));
        }
    }

    // this is to add a word to a specific list
    @PostMapping("/words")
    public ResponseEntity<?> addWord(@RequestBody WordInList word) {
        try {
            Optional<WordInList> existingWord = wordInListRepository
                    .findByUserIdAndListIdAndWord(word.getUserId(), word.getListId(), word.getWord());
            if (existingWord.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "This word already exists in this list"));
            }
            WordInList savedWord = wordInListRepository.save(word);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Word saved successfully");
            response.put("word", savedWord);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save word"));
        }
    }

    @DeleteMapping("/words/{wordId}")
    public ResponseEntity<?> deleteWord(@PathVariable("wordId") String wordId) {
        try {
            wordInListRepository.deleteById(wordId);
            return ResponseEntity.ok(Map.of("message", "Word deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete word"));
        }
    }

    // this is to update a word in a specific list
    @PutMapping("/words/{wordId}")
    public ResponseEntity<?> updateWord(@PathVariable("wordId") String wordId, @RequestBody WordInList updatedWord) {
        try {
            Optional<WordInList> wordOpt = wordInListRepository.findById(wordId);
            if (wordOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            WordInList word = wordOpt.get();
            word.setWord(updatedWord.getWord());
            word.setDefinition(updatedWord.getDefinition());
            wordInListRepository.save(word);
            return ResponseEntity.ok(Map.of("message", "Word updated successfully", "word", word));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update word"));
        }
    }

    // delete a vocab list and all words in it
    @DeleteMapping("/lists/{userId}/{listId}")
    public ResponseEntity<?> deleteList(@PathVariable("userId") String userId, @PathVariable("listId") String listId) {
        try {
            // remove all words in the list in one repository operation
            wordInListRepository.deleteByListId(listId);

            // delete the list itself
            Optional<VocabList> listOpt = vocabListRepository.findById(listId);
            if (listOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "List not found"));
            }

            VocabList list = listOpt.get();
            if (!list.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Not authorized to delete this list"));
            }

            vocabListRepository.deleteById(listId);
            return ResponseEntity.ok(Map.of("message", "List and its words deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete list"));
        }
    }
}