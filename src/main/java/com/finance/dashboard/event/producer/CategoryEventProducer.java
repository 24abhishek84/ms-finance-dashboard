package com.finance.dashboard.event.producer;

import com.finance.dashboard.event.CategoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryEventProducer {

    private final KafkaTemplate<String, CategoryEvent> kafkaTemplate;

    @Value("${kafka.topics.category:category-events}")
    private String categoryTopic;

    public void publishCategoryEvent(CategoryEvent event) {
        try {
            log.info("Publishing category event: eventId={}, type={}, categoryId={}",
                event.getEventId(), event.getEventType(), event.getCategoryId());

            kafkaTemplate.send(categoryTopic, event.getEventId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish category event: eventId={}", event.getEventId(), ex);
                    } else {
                        log.info("Successfully published category event: eventId={}, partition={}, offset={}",
                            event.getEventId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    }
                });
        } catch (Exception e) {
            log.error("Exception occurred while publishing category event", e);
            throw new RuntimeException("Failed to publish category event", e);
        }
    }
}
