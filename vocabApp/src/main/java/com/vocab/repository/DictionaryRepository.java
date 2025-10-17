package com.vocab.repository;

import com.vocab.model.Dictionary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DictionaryRepository extends MongoRepository<Dictionary, String> {
    Dictionary findByWord(String word);
    
    @Query(value = "{}", fields = "{}")
    Dictionary findRandomWord();
}