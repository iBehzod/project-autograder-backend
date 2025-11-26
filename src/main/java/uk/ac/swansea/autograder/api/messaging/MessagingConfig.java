package uk.ac.swansea.autograder.api.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import uk.ac.swansea.autograder.api.services.SubmissionExecutionService;

/**
 * Redis message queue configuration for asynchronous submission processing.
 * 
 * Message queues are ideal for requests which may take a long time to process.
 * Enables asynchronously executing tests without blocking REST response time.
 * Also allows using third party code execution engines and balancing load during peak hours.
 */
@Configuration
public class MessagingConfig {
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("submissionId"));

        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(SubmissionReceiver submissionReceiver) {
        return new MessageListenerAdapter(submissionReceiver, "receiveMessage");
    }

    @Bean
    SubmissionReceiver receiver(SubmissionExecutionService submissionExecutionService) {
        return new SubmissionReceiver(submissionExecutionService);
    }

    @Bean
    StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
