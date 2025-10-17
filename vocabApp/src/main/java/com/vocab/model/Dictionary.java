package com.vocab.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "dictionary")
public class Dictionary {
    
    @Id
    private String id;
    private String word;
    private String shortdef;
    private String category;
    private String partOfSpeech;
    
    public Dictionary() {}
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getWord() {
        return word;
    }
    
    public void setWord(String word) {
        this.word = word;
    }
    
    public String getShortdef() {
        return shortdef;
    }
    
    public void setShortdef(String shortdef) {
        this.shortdef = shortdef;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getPartOfSpeech() {
        return partOfSpeech;
    }
    
    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }
}