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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatThreadRepository threadRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatReadStatusRepository readStatusRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    public ChatServiceImpl(ChatThreadRepository threadRepository,
                           ChatMessageRepository messageRepository,
                           UserRepository userRepository,
                           ChatReadStatusRepository readStatusRepository) {
        this.threadRepository = threadRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.readStatusRepository = readStatusRepository;
    }

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
    public List<ChatMessageResponse> getMessagesForThread(Long threadId, User currentUser) {
        ChatThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new EntityNotFoundException("Chat thread not found: " + threadId));
        validateParticipation(thread, currentUser);
        
        // Отримуємо останні 20 повідомлень (відсортовані за датою створення DESC)
        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        List<ChatMessage> messages = messageRepository.findByThreadOrderByCreatedAtDesc(thread, pageable);
        
        // Реверсуємо список, щоб найстаріші були першими (для правильного відображення в чаті)
        Collections.reverse(messages);
        
        // Відмічаємо повідомлення як прочитані
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
        entityManager.flush();
    }

    @Override
    @Transactional // Явно вказуємо транзакцію для методу
    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        try {
            System.out.println("=== sendMessage called ===");
            System.out.println("Request: " + request);
            System.out.println("SenderUserId: " + request.getSenderUserId());
            System.out.println("ThreadId: " + request.getThreadId());
            System.out.println("RecipientUserId: " + request.getRecipientUserId());
            System.out.println("Content: " + request.getContent());
            
            // Перезавантажуємо User в межах транзакції, щоб він був managed
            User sender = userRepository.findById(request.getSenderUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Sender not found: " + request.getSenderUserId()));
            System.out.println("Sender found: " + sender.getEmail());
            
            // Переконаємося, що User прив'язаний до сесії
            if (!entityManager.contains(sender)) {
                sender = entityManager.merge(sender);
                System.out.println("Sender merged into persistence context");
            }

            ChatThread thread = resolveThread(request, sender);
            System.out.println("Thread resolved: " + thread.getId());
            
            // Переконаємося, що Thread прив'язаний до сесії
            if (!entityManager.contains(thread)) {
                thread = entityManager.merge(thread);
                System.out.println("Thread merged into persistence context");
            }
            
            validateParticipation(thread, sender);
            System.out.println("Participation validated");

            // Перевіряємо, що всі дані валідні перед створенням повідомлення
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Message content cannot be empty");
            }
            
            // Перевіряємо, що thread і sender мають ID
            if (thread.getId() == null) {
                throw new IllegalStateException("Thread ID is null - thread not persisted!");
            }
            if (sender.getId() == null) {
                throw new IllegalStateException("Sender ID is null - sender not persisted!");
            }

            ChatMessage message = new ChatMessage();
            message.setThread(thread);
            message.setSender(sender);
            message.setContent(request.getContent().trim());
            message.setMessageType(ChatMessage.MessageType.TEXT);
            
            System.out.println("Message created, saving...");
            System.out.println("Message thread ID: " + (message.getThread() != null ? message.getThread().getId() : "NULL"));
            System.out.println("Message sender ID: " + (message.getSender() != null ? message.getSender().getId() : "NULL"));
            System.out.println("Message content: " + message.getContent());
            System.out.println("Message type: " + message.getMessageType());
            
            message = messageRepository.save(message);
            System.out.println("Message saved with ID: " + message.getId());
            
            // Явно виконуємо flush, щоб переконатися, що зміни збережені
            try {
                entityManager.flush();
                System.out.println("Flush successful");
            } catch (Exception flushError) {
                System.err.println("ERROR during flush: " + flushError.getMessage());
                flushError.printStackTrace();
                throw flushError;
            }
            
            // Перевіряємо, що повідомлення дійсно збережено
            ChatMessage savedMessage = messageRepository.findById(message.getId()).orElse(null);
            if (savedMessage == null) {
                System.err.println("ERROR: Message was not saved to database! ID: " + message.getId());
                throw new IllegalStateException("Message was not persisted to database");
            } else {
                System.out.println("✅ Message confirmed in database: " + savedMessage.getId() + ", content: " + savedMessage.getContent());
                System.out.println("✅ Message thread ID: " + (savedMessage.getThread() != null ? savedMessage.getThread().getId() : "NULL"));
                System.out.println("✅ Message sender ID: " + (savedMessage.getSender() != null ? savedMessage.getSender().getId() : "NULL"));
            }

            // Оновлюємо тред (updatedAt оновлюється автоматично через @UpdateTimestamp)
            threadRepository.save(thread);
            entityManager.flush(); // Явно виконуємо flush для треду
            System.out.println("Thread updated");

            // Переконаємося, що thread і sender завантажені перед створенням response
            // Це запобігає LazyInitializationException
            System.out.println("Preparing response...");
            System.out.println("Message thread ID: " + message.getThread().getId());
            System.out.println("Message sender ID: " + message.getSender().getId());
            System.out.println("Message sender email: " + message.getSender().getEmail());
            
            ChatMessageResponse response = ChatMessageResponse.from(message);
            System.out.println("✅ Response created: " + response);
            System.out.println("Response ID: " + response.getId());
            System.out.println("Response threadId: " + response.getThreadId());
            System.out.println("Response senderId: " + response.getSenderId());
            System.out.println("Response content: " + response.getContent());
            
            return response;
        } catch (Exception e) {
            System.err.println("ERROR in sendMessage: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private ChatThread createThread(User currentUser, User otherUser) {
        System.out.println("Creating new thread between: " + currentUser.getEmail() + " and " + otherUser.getEmail());
        
        // Перезавантажуємо користувачів в межах транзакції, щоб вони були managed
        // Це необхідно, якщо вони були завантажені в іншій транзакції
        User managedCurrentUser = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Current user not found: " + currentUser.getId()));
        User managedOtherUser = userRepository.findById(otherUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Other user not found: " + otherUser.getId()));
        
        ChatThread thread = new ChatThread();
        thread.getParticipants().add(managedCurrentUser);
        thread.getParticipants().add(managedOtherUser);
        
        thread = threadRepository.save(thread);
        entityManager.flush(); // Явно виконуємо flush для нового треду
        
        // Перевіряємо, що thread дійсно збережено
        if (thread.getId() == null) {
            throw new IllegalStateException("Thread ID is null after save!");
        }
        
        System.out.println("✅ New thread created with ID: " + thread.getId());
        System.out.println("✅ Thread participants count: " + thread.getParticipants().size());
        return thread;
    }

    private ChatThread resolveThread(ChatMessageRequest request, User sender) {
        if (request.getThreadId() != null) {
            ChatThread thread = threadRepository.findById(request.getThreadId())
                    .orElseThrow(() -> new EntityNotFoundException("Chat thread not found: " + request.getThreadId()));
            // Переконаємося, що thread прив'язаний до сесії
            if (!entityManager.contains(thread)) {
                thread = entityManager.merge(thread);
            }
            return thread;
        }

        if (request.getRecipientUserId() == null) {
            throw new IllegalArgumentException("Either threadId or recipientUserId must be provided");
        }

        User recipient = userRepository.findById(request.getRecipientUserId())
                .orElseThrow(() -> new EntityNotFoundException("Recipient not found: " + request.getRecipientUserId()));
        
        // Переконаємося, що recipient прив'язаний до сесії
        final User managedRecipient;
        if (!entityManager.contains(recipient)) {
            managedRecipient = entityManager.merge(recipient);
        } else {
            managedRecipient = recipient;
        }

        ChatThread thread = threadRepository.findThreadBetween(sender, managedRecipient)
                .orElseGet(() -> {
                    ChatThread newThread = createThread(sender, managedRecipient);
                    // Переконаємося, що новий thread прив'язаний до сесії
                    if (!entityManager.contains(newThread)) {
                        newThread = entityManager.merge(newThread);
                    }
                    return newThread;
                });
        
        // Переконаємося, що thread прив'язаний до сесії
        if (!entityManager.contains(thread)) {
            thread = entityManager.merge(thread);
            System.out.println("Thread merged into persistence context");
        }
        
        System.out.println("✅ Thread resolved: ID=" + thread.getId() + ", participants=" + thread.getParticipants().size());
        return thread;
    }

    @Override
    @Transactional
    public void deleteThread(Long threadId, User user) {
        ChatThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new EntityNotFoundException("Chat thread not found: " + threadId));
        
        // Перевіряємо, що користувач є учасником треду
        validateParticipation(thread, user);
        
        // Видаляємо всі статуси прочитання для цього треду
        List<ChatReadStatus> readStatuses = readStatusRepository.findByThread(thread);
        if (!readStatuses.isEmpty()) {
            readStatusRepository.deleteAll(readStatuses);
        }
        
        // Видаляємо всі повідомлення треду
        List<ChatMessage> messages = messageRepository.findByThread(thread);
        if (!messages.isEmpty()) {
            messageRepository.deleteAll(messages);
        }
        
        // Видаляємо сам тред
        threadRepository.delete(thread);
        entityManager.flush();
    }
    
    @Override
    @Transactional
    public void deleteMessage(Long messageId, User user) {
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Chat message not found: " + messageId));
        
        // Перевіряємо, що користувач є автором повідомлення
        if (!message.getSender().getId().equals(user.getId())) {
            throw new IllegalStateException("You can only delete your own messages");
        }
        
        // Перевіряємо, що користувач є учасником треду
        validateParticipation(message.getThread(), user);
        
        // Видаляємо повідомлення
        messageRepository.delete(message);
        entityManager.flush();
    }

    @Override
    @Transactional
    public void markThreadAsRead(Long threadId, User user) {
        ChatThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new EntityNotFoundException("Chat thread not found: " + threadId));
        validateParticipation(thread, user);
        
        // Знаходимо останнє повідомлення в треді
        List<ChatMessage> messages = messageRepository.findByThreadOrderByCreatedAtAsc(thread);
        if (!messages.isEmpty()) {
            ChatMessage lastMessage = messages.get(messages.size() - 1);
            markAsRead(thread, user, lastMessage);
        }
    }

    private void validateParticipation(ChatThread thread, User user) {
        boolean participant = thread.getParticipants()
                .stream()
                .anyMatch(p -> p.getId().equals(user.getId()));

        if (!participant) {
            throw new IllegalStateException("User is not a participant of the thread");
        }
    }
}

