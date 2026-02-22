package com.example.step_project_beck_spring.service.chat;

import com.example.step_project_beck_spring.dto.chat.ChatMessageRequest;
import com.example.step_project_beck_spring.dto.chat.ChatMessageResponse;
import com.example.step_project_beck_spring.dto.chat.ChatThreadResponse;
import com.example.step_project_beck_spring.entities.ChatMessage;
import com.example.step_project_beck_spring.entities.ChatReadStatus;
import com.example.step_project_beck_spring.entities.ChatThread;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.ChatMessageRepository;
import com.example.step_project_beck_spring.repository.ChatReadStatusRepository;
import com.example.step_project_beck_spring.repository.ChatThreadRepository;
import com.example.step_project_beck_spring.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatThreadRepository threadRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatReadStatusRepository readStatusRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public ChatThreadResponse getOrCreateThread(User currentUser, User otherUser) {
        ChatThread thread = threadRepository.findThreadBetween(currentUser, otherUser)
                .orElseGet(() -> createThread(currentUser, otherUser));
        Long unreadCount = readStatusRepository.countUnreadMessages(thread, currentUser);
        return ChatThreadResponse.from(thread, currentUser, unreadCount);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "chatThreads", key = "#user.id")  // ← Оптимізація: кеш тредів по userId
    public List<ChatThreadResponse> getThreadsForUser(User user) {
        return threadRepository.findAllByParticipant(user)
                .stream()
                .map(thread -> {
                    Long unreadCount = readStatusRepository.countUnreadMessages(thread, user);
                    return ChatThreadResponse.from(thread, user, unreadCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ChatMessageResponse> getMessagesForThread(UUID threadId, User currentUser) {
        ChatThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new EntityNotFoundException("Chat thread not found: " + threadId));
        validateParticipation(thread, currentUser);

        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        List<ChatMessage> messages = messageRepository.findByThreadOrderByCreatedAtDesc(thread, pageable);

        Collections.reverse(messages);

        if (!messages.isEmpty()) {
            ChatMessage lastMessage = messages.get(messages.size() - 1);
            markAsRead(thread, currentUser, lastMessage);
        }

        return messages.stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }

    private void markAsRead(ChatThread thread, User user, ChatMessage lastMessage) {
        ChatReadStatus readStatus = readStatusRepository.findByThreadAndUser(thread, user)
                .orElse(new ChatReadStatus(thread, user, lastMessage));

        readStatus.setLastReadMessage(lastMessage);
        readStatusRepository.save(readStatus);
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        try {
            log.info("sendMessage: request for threadId={}, recipient={}",
                    request.getThreadId(), request.getRecipientUserId());

            User sender = userRepository.findById(request.getSenderUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Sender not found: " + request.getSenderUserId()));

            if (!entityManager.contains(sender)) {
                sender = entityManager.merge(sender);
            }

            ChatThread thread = resolveThread(request, sender);

            if (!entityManager.contains(thread)) {
                thread = entityManager.merge(thread);
            }

            validateParticipation(thread, sender);

            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Message content cannot be empty");
            }

            ChatMessage message = new ChatMessage();
            message.setThread(thread);
            message.setSender(sender);
            message.setContent(request.getContent().trim());
            message.setMessageType(ChatMessage.MessageType.TEXT);

            message = messageRepository.save(message);

            // Повідомлення про MESSAGE (якщо потрібно — додай тут виклик NotificationService)

            return ChatMessageResponse.from(message);
        } catch (Exception e) {
            log.error("Error in sendMessage: {}", e.getMessage(), e);
            throw e;
        }
    }

    private ChatThread createThread(User currentUser, User otherUser) {
        log.debug("Creating new thread between {} and {}", currentUser.getEmail(), otherUser.getEmail());

        User managedCurrentUser = entityManager.contains(currentUser) ? currentUser : entityManager.merge(currentUser);
        User managedOtherUser = entityManager.contains(otherUser) ? otherUser : entityManager.merge(otherUser);

        ChatThread thread = new ChatThread();
        thread.getParticipants().add(managedCurrentUser);
        thread.getParticipants().add(managedOtherUser);

        return threadRepository.save(thread);
    }

    private ChatThread resolveThread(ChatMessageRequest request, User sender) {
        if (request.getThreadId() != null) {
            return threadRepository.findById(request.getThreadId())
                    .orElseThrow(() -> new EntityNotFoundException("Chat thread not found: " + request.getThreadId()));
        }

        if (request.getRecipientUserId() == null) {
            throw new IllegalArgumentException("Either threadId or recipientUserId must be provided");
        }

        User recipient = userRepository.findById(request.getRecipientUserId())
                .orElseThrow(() -> new EntityNotFoundException("Recipient not found: " + request.getRecipientUserId()));

        return threadRepository.findThreadBetween(sender, recipient)
                .orElseGet(() -> createThread(sender, recipient));
    }

    @Override
    @Transactional
    public void deleteThread(UUID threadId, User user) {
        ChatThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new EntityNotFoundException("Chat thread not found: " + threadId));

        validateParticipation(thread, user);

        readStatusRepository.deleteAll(readStatusRepository.findByThread(thread));
        messageRepository.deleteAll(messageRepository.findByThread(thread));
        threadRepository.delete(thread);
    }

    @Override
    @Transactional
    public void deleteMessage(UUID messageId, User user) {
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Chat message not found: " + messageId));

        if (!message.getSender().getId().equals(user.getId())) {
            throw new IllegalStateException("You can only delete your own messages");
        }

        validateParticipation(message.getThread(), user);

        messageRepository.delete(message);
    }

    @Override
    @Transactional
    public void markThreadAsRead(UUID threadId, User user) {
        ChatThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new EntityNotFoundException("Chat thread not found: " + threadId));

        validateParticipation(thread, user);

        List<ChatMessage> messages = messageRepository.findByThreadOrderByCreatedAtAsc(thread);

        if (!messages.isEmpty()) {
            ChatMessage lastMessage = messages.get(messages.size() - 1);
            markAsRead(thread, user, lastMessage);
        }
    }

    private void validateParticipation(ChatThread thread, User user) {
        boolean participant = thread.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(user.getId()));

        if (!participant) {
            throw new IllegalStateException("User is not a participant of the thread");
        }
    }
}