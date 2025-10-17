package com.vocab.repository;

import com.vocab.model.VocabList;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface VocabListRepository extends MongoRepository<VocabList, String> {
    List<VocabList> findByUserId(String userId);
    Optional<VocabList> findFirstByUserIdOrderByCreatedAtAsc(String userId);
}