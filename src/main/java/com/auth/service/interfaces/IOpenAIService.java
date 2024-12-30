package com.auth.service.interfaces;

import java.util.List;

public interface IOpenAIService {
    List<Object> findSimilarDocuments(String question);
}

