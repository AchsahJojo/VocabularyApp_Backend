package com.vocab.controller;

import com.vocab.model.Dictionary;
import com.vocab.repository.DictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DictionaryControllerTest {

    @Mock
    private DictionaryRepository dictionaryRepository;

    @InjectMocks
    private DictionaryController dictionaryController;

    private Dictionary testWord;
    private List<Dictionary> wordList;

    @BeforeEach
    void setUp() {
        testWord = new Dictionary();
        testWord.setId("word123");
        testWord.setWord("example");
        testWord.setShortdef("a thing characteristic of its kind or illustrating a general rule");
        testWord.setCategory("general");
        testWord.setPartOfSpeech("noun");

        Dictionary word2 = new Dictionary();
        word2.setId("word456");
        word2.setWord("test");
        word2.setShortdef("a procedure intended to establish quality or performance");
        word2.setCategory("general");
        word2.setPartOfSpeech("noun");

        wordList = Arrays.asList(testWord, word2);
    }

    // ==================== GET RANDOM WORD TESTS ====================

    @Test
    void testGetRandomWord_Success() {
        // Arrange
        when(dictionaryRepository.findAll()).thenReturn(wordList);

        // Act
        ResponseEntity<?> response = dictionaryController.getRandomWord();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Dictionary);
        
        Dictionary returnedWord = (Dictionary) response.getBody();
        assertTrue(returnedWord.getWord().equals("example") || returnedWord.getWord().equals("test"));

        verify(dictionaryRepository).findAll();
    }

    @Test
    void testGetRandomWord_EmptyDictionary() {
        // Arrange
        when(dictionaryRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<?> response = dictionaryController.getRandomWord();

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("No words in dictionary", body.get("error"));

        verify(dictionaryRepository).findAll();
    }

    @Test
    void testGetRandomWord_RepositoryThrowsException() {
        // Arrange
        when(dictionaryRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = dictionaryController.getRandomWord();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Failed to fetch random word", body.get("error"));

        verify(dictionaryRepository).findAll();
    }

    // ==================== GET WORD DEFINITION TESTS ====================

    @Test
    void testGetWordDefinition_Success() {
        // Arrange
        when(dictionaryRepository.findByWord(anyString())).thenReturn(testWord);

        // Act
        ResponseEntity<?> response = dictionaryController.getWordDefinition("example");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Dictionary);
        
        Dictionary returnedWord = (Dictionary) response.getBody();
        assertEquals("example", returnedWord.getWord());
        assertEquals("a thing characteristic of its kind or illustrating a general rule", returnedWord.getShortdef());

        verify(dictionaryRepository).findByWord("example");
    }

    @Test
    void testGetWordDefinition_CaseInsensitive() {
        // Arrange
        when(dictionaryRepository.findByWord("example")).thenReturn(testWord);

        // Act
        ResponseEntity<?> response = dictionaryController.getWordDefinition("EXAMPLE");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        verify(dictionaryRepository).findByWord("example");
    }

    @Test
    void testGetWordDefinition_WordNotFound() {
        // Arrange
        when(dictionaryRepository.findByWord(anyString())).thenReturn(null);

        // Act
        ResponseEntity<?> response = dictionaryController.getWordDefinition("nonexistent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Word not found", body.get("error"));

        verify(dictionaryRepository).findByWord("nonexistent");
    }

    @Test
    void testGetWordDefinition_RepositoryThrowsException() {
        // Arrange
        when(dictionaryRepository.findByWord(anyString())).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = dictionaryController.getWordDefinition("example");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Failed to fetch word", body.get("error"));
    }

    // ==================== ADD WORD TESTS ====================

    @Test
    void testAddWord_Success() {
        // Arrange
        Dictionary newWord = new Dictionary();
        newWord.setWord("NewWord");
        newWord.setShortdef("A new definition");
        newWord.setCategory("test");
        newWord.setPartOfSpeech("noun");

        Dictionary savedWord = new Dictionary();
        savedWord.setId("newWordId");
        savedWord.setWord("newword");
        savedWord.setShortdef(newWord.getShortdef());
        savedWord.setCategory(newWord.getCategory());
        savedWord.setPartOfSpeech(newWord.getPartOfSpeech());

        when(dictionaryRepository.save(any(Dictionary.class))).thenReturn(savedWord);

        // Act
        ResponseEntity<?> response = dictionaryController.addWord(newWord);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Dictionary);
        
        Dictionary returnedWord = (Dictionary) response.getBody();
        assertEquals("newword", returnedWord.getWord());
        assertEquals("newWordId", returnedWord.getId());

        verify(dictionaryRepository).save(any(Dictionary.class));
    }

    @Test
    void testAddWord_ConvertsToLowerCase() {
        // Arrange
        Dictionary newWord = new Dictionary();
        newWord.setWord("UPPERCASE");
        newWord.setShortdef("Definition");

        when(dictionaryRepository.save(any(Dictionary.class))).thenAnswer(invocation -> {
            Dictionary savedDict = invocation.getArgument(0);
            savedDict.setId("id123");
            return savedDict;
        });

        // Act
        ResponseEntity<?> response = dictionaryController.addWord(newWord);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        Dictionary returnedWord = (Dictionary) response.getBody();
        assertEquals("uppercase", returnedWord.getWord());
    }

    @Test
    void testAddWord_RepositoryThrowsException() {
        // Arrange
        Dictionary newWord = new Dictionary();
        newWord.setWord("test");
        newWord.setShortdef("definition");

        when(dictionaryRepository.save(any(Dictionary.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = dictionaryController.addWord(newWord);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Failed to add word", body.get("error"));
    }
}