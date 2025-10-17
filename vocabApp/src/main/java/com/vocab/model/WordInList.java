package com.vocab.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "wordinlists")
public class WordInList {
    
    @Id
    private String id;
    private String listId;
    private String userId;
    private String word;
    private String definition;
    private String category;
    private LocalDateTime addedAt;
    
    public WordInList() {
        this.addedAt = LocalDateTime.now();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getListId() {
        return listId;
    }
    
    public void setListId(String listId) {
        this.listId = listId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getWord() {
        return word;
    }
    
    public void setWord(String word) {
        this.word = word;
    }
    
    public String getDefinition() {
        return definition;
    }
    
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    
    public String getCategories() {
        return category;
    }
    
    public void setCategories(String category) {
        this.category = category;
    }
    
    public LocalDateTime getAddedAt() {
        return addedAt;
    }
    
    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}