package com.vocab.controller;

import com.vocab.model.VocabList;
import com.vocab.model.WordInList;
import com.vocab.repository.VocabListRepository;
import com.vocab.repository.WordInListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VocabControllerTest {

    @Mock
    private VocabListRepository vocabListRepository;

    @Mock
    private WordInListRepository wordInListRepository;

    @InjectMocks
    private VocabController vocabController;

    private VocabList testList;
    private WordInList testWord;
    private String userId;
    private String listId;

    @BeforeEach
    void setUp() {
        userId = "user123";
        listId = "list456";

        testList = new VocabList();
        testList.setId(listId);
        testList.setUserId(userId);
        testList.setListName("My Vocab List");

        testWord = new WordInList();
        testWord.setId("word789");
        testWord.setUserId(userId);
        testWord.setListId(listId);
        testWord.setWord("eloquent");
        testWord.setDefinition("fluent or persuasive in speaking or writing");
        testWord.setCategories("adjective");
    }

    // ==================== GET ALL LISTS TESTS ====================

    @Test
    void testGetAllLists_Success() {
        // Arrange
        List<VocabList> lists = Arrays.asList(testList);
        when(vocabListRepository.findByUserId(userId)).thenReturn(lists);

        // Act
        ResponseEntity<?> response = vocabController.getAllLists(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        List<VocabList> returnedLists = (List<VocabList>) response.getBody();
        assertEquals(1, returnedLists.size());
        assertEquals("My Vocab List", returnedLists.get(0).getListName());

        verify(vocabListRepository).findByUserId(userId);
    }

    @Test
    void testGetAllLists_EmptyList() {
        // Arrange
        when(vocabListRepository.findByUserId(userId)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<?> response = vocabController.getAllLists(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        List<VocabList> returnedLists = (List<VocabList>) response.getBody();
        assertTrue(returnedLists.isEmpty());
    }

    @Test
    void testGetAllLists_RepositoryThrowsException() {
        // Arrange
        when(vocabListRepository.findByUserId(userId)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = vocabController.getAllLists(userId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Failed to fetch lists", body.get("error"));
    }

    // ==================== GET LISTS EXCLUDING HISTORY TESTS ====================

    @Test
    void testGetListsExcludingHistory_Success() {
        // Arrange
        VocabList historyList = new VocabList();
        historyList.setId("historyId");
        historyList.setUserId(userId);
        historyList.setListName("Vocab Word History");

        VocabList regularList = new VocabList();
        regularList.setId("regularId");
        regularList.setUserId(userId);
        regularList.setListName("Regular List");

        List<VocabList> allLists = new ArrayList<>(Arrays.asList(historyList, regularList));

        when(vocabListRepository.findFirstByUserIdOrderByCreatedAtAsc(userId))
                .thenReturn(Optional.of(historyList));
        when(vocabListRepository.findByUserId(userId)).thenReturn(allLists);

        // Act
        ResponseEntity<?> response = vocabController.getListsExcludingHistory(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        
        @SuppressWarnings("unchecked")
        List<VocabList> lists = (List<VocabList>) body.get("lists");
        assertEquals(1, lists.size());
        assertEquals("Regular List", lists.get(0).getListName());
        assertEquals("historyId", body.get("vocabHistoryId"));
    }

    @Test
    void testGetListsExcludingHistory_NoHistoryList() {
        // Arrange
        when(vocabListRepository.findFirstByUserIdOrderByCreatedAtAsc(userId))
                .thenReturn(Optional.empty());
        when(vocabListRepository.findByUserId(userId)).thenReturn(Arrays.asList(testList));

        // Act
        ResponseEntity<?> response = vocabController.getListsExcludingHistory(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNull(body.get("vocabHistoryId"));
    }

    // ==================== CREATE LIST TESTS ====================

    @Test
    void testCreateList_Success() {
        // Arrange
        VocabList newList = new VocabList();
        newList.setUserId(userId);
        newList.setListName("New Test List");

        when(vocabListRepository.save(any(VocabList.class))).thenReturn(testList);

        // Act
        ResponseEntity<?> response = vocabController.createList(newList);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("List created successfully", body.get("message"));
        assertNotNull(body.get("list"));

        verify(vocabListRepository).save(any(VocabList.class));
    }

    @Test
    void testCreateList_EmptyListName() {
        // Arrange
        VocabList newList = new VocabList();
        newList.setUserId(userId);
        newList.setListName("");

        // Act
        ResponseEntity<?> response = vocabController.createList(newList);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Please enter a list name", body.get("error"));

        verify(vocabListRepository, never()).save(any(VocabList.class));
    }

    @Test
    void testCreateList_NullListName() {
        // Arrange
        VocabList newList = new VocabList();
        newList.setUserId(userId);
        newList.setListName(null);

        // Act
        ResponseEntity<?> response = vocabController.createList(newList);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(vocabListRepository, never()).save(any(VocabList.class));
    }

    // ==================== GET WORDS IN LIST TESTS ====================

    @Test
    void testGetWordsInList_Success() {
        // Arrange
        List<WordInList> words = Arrays.asList(testWord);
        when(vocabListRepository.findById(listId)).thenReturn(Optional.of(testList));
        when(wordInListRepository.findByUserIdAndListId(userId, listId)).thenReturn(words);

        // Act
        ResponseEntity<?> response = vocabController.getWordsInList(userId, listId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("My Vocab List", body.get("listName"));
        
        @SuppressWarnings("unchecked")
        List<WordInList> returnedWords = (List<WordInList>) body.get("words");
        assertEquals(1, returnedWords.size());
        assertEquals("eloquent", returnedWords.get(0).getWord());

        verify(vocabListRepository).findById(listId);
        verify(wordInListRepository).findByUserIdAndListId(userId, listId);
    }

    @Test
    void testGetWordsInList_EmptyList() {
        // Arrange
        when(vocabListRepository.findById(listId)).thenReturn(Optional.of(testList));
        when(wordInListRepository.findByUserIdAndListId(userId, listId)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<?> response = vocabController.getWordsInList(userId, listId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        
        @SuppressWarnings("unchecked")
        List<WordInList> words = (List<WordInList>) body.get("words");
        assertTrue(words.isEmpty());
    }

    // ==================== ADD WORD TESTS ====================

    @Test
    void testAddWord_Success() {
        // Arrange
        WordInList newWord = new WordInList();
        newWord.setUserId(userId);
        newWord.setListId(listId);
        newWord.setWord("verbose");
        newWord.setDefinition("using or expressed in more words than needed");

        when(wordInListRepository.findByUserIdAndListIdAndWord(userId, listId, "verbose"))
                .thenReturn(Optional.empty());
        when(wordInListRepository.save(any(WordInList.class))).thenReturn(newWord);

        // Act
        ResponseEntity<?> response = vocabController.addWord(newWord);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Word saved successfully", body.get("message"));
        assertNotNull(body.get("word"));

        verify(wordInListRepository).save(any(WordInList.class));
    }

    @Test
    void testAddWord_DuplicateWord() {
        // Arrange
        when(wordInListRepository.findByUserIdAndListIdAndWord(userId, listId, "eloquent"))
                .thenReturn(Optional.of(testWord));

        // Act
        ResponseEntity<?> response = vocabController.addWord(testWord);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("This word already exists in this list", body.get("error"));

        verify(wordInListRepository, never()).save(any(WordInList.class));
    }

    // ==================== DELETE WORD TESTS ====================

    @Test
    void testDeleteWord_Success() {
        // Arrange
        String wordId = "word789";
        doNothing().when(wordInListRepository).deleteById(wordId);

        // Act
        ResponseEntity<?> response = vocabController.deleteWord(wordId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Word deleted successfully", body.get("message"));

        verify(wordInListRepository).deleteById(wordId);
    }

    @Test
    void testDeleteWord_RepositoryThrowsException() {
        // Arrange
        String wordId = "word789";
        doThrow(new RuntimeException("Database error")).when(wordInListRepository).deleteById(wordId);

        // Act
        ResponseEntity<?> response = vocabController.deleteWord(wordId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Failed to delete word", body.get("error"));
    }

    // ==================== UPDATE WORD TESTS ====================

    @Test
    void testUpdateWord_Success() {
        // Arrange
        WordInList updatedWord = new WordInList();
        updatedWord.setWord("eloquent");
        updatedWord.setDefinition("Updated definition");

        when(wordInListRepository.findById("word789")).thenReturn(Optional.of(testWord));
        when(wordInListRepository.save(any(WordInList.class))).thenReturn(testWord);

        // Act
        ResponseEntity<?> response = vocabController.updateWord("word789", updatedWord);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Word updated successfully", body.get("message"));
        assertNotNull(body.get("word"));

        verify(wordInListRepository).save(any(WordInList.class));
    }

    @Test
    void testUpdateWord_NotFound() {
        // Arrange
        WordInList updatedWord = new WordInList();
        updatedWord.setWord("eloquent");
        updatedWord.setDefinition("Updated definition");

        when(wordInListRepository.findById("word789")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = vocabController.updateWord("word789", updatedWord);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(wordInListRepository, never()).save(any(WordInList.class));
    }

    @Test
    void testUpdateWord_RepositoryThrowsException() {
        // Arrange
        WordInList updatedWord = new WordInList();
        updatedWord.setWord("eloquent");
        updatedWord.setDefinition("Updated definition");

        when(wordInListRepository.findById("word789")).thenReturn(Optional.of(testWord));
        when(wordInListRepository.save(any(WordInList.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = vocabController.updateWord("word789", updatedWord);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Failed to update word", body.get("error"));
    }
}