package com.finance.dashboard.service;

import com.finance.dashboard.dto.TransactionRequestDTO;
import com.finance.dashboard.dto.TransactionResponseDTO;
import com.finance.dashboard.entity.Category;
import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.repository.CategoryRepository;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    
    public TransactionResponseDTO createTransaction(Long userId, TransactionRequestDTO request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        Transaction transaction = Transaction.builder()
            .description(request.getDescription())
            .amount(request.getAmount())
            .transactionDate(request.getTransactionDate())
            .notes(request.getNotes())
            .user(user)
            .category(category)
            .build();
        
        Transaction saved = transactionRepository.save(transaction);
        return mapToResponseDTO(saved);
    }
    
    @Cacheable(value = "userTransactions", key = "#userId")
    public List<TransactionResponseDTO> getUserTransactions(Long userId) {
        return transactionRepository.findByUserId(userId).stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    public List<TransactionResponseDTO> getTransactionsByDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findAll().stream()
            .filter(transaction -> isWithinUserAndDateRange(transaction, userId, startDate, endDate))
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }
    
    public TransactionResponseDTO updateTransaction(
            Long transactionId, TransactionRequestDTO request) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNotes(request.getNotes());
        transaction.setCategory(category);
        transaction.setUpdatedAt(LocalDateTime.now());
        
        Transaction updated = transactionRepository.save(transaction);
        return mapToResponseDTO(updated);
    }
    
    public void deleteTransaction(Long transactionId) {
        transactionRepository.deleteById(transactionId);
    }
    
    private TransactionResponseDTO mapToResponseDTO(Transaction transaction) {
        return TransactionResponseDTO.builder()
            .id(transaction.getId())
            .description(transaction.getDescription())
            .amount(transaction.getAmount())
            .transactionDate(transaction.getTransactionDate())
            .notes(transaction.getNotes())
            .userId(transaction.getUser().getId())
            .categoryId(transaction.getCategory().getId())
            .categoryName(transaction.getCategory().getName())
            .createdAt(transaction.getCreatedAt())
            .updatedAt(transaction.getUpdatedAt())
            .build();
    }

    private boolean isWithinUserAndDateRange(Transaction transaction, Long userId, LocalDate startDate, LocalDate endDate) {
        return transaction.getUser().getId().equals(userId) &&
               !transaction.getTransactionDate().isBefore(startDate) &&
               !transaction.getTransactionDate().isAfter(endDate);
    }
}
