package uk.ac.swansea.autograder.api.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time submission test results.
 * 
 * <h3>Connection Model: Broadcast</h3>
 * Uses a simple broadcast model where all clients subscribed to /topic/test-results
 * receive the same messages. This is intentional and suitable for the current use case.
 * 
 * <h3>Multiple Connection Handling:</h3>
 * <ul>
 *   <li>If a student opens multiple browser tabs, all tabs receive broadcast messages</li>
 *   <li>Each tab can filter messages client-side based on submissionId</li>
 *   <li>Authorization is enforced at the message handler level (WebSocketController)</li>
 * </ul>
 * 
 * <h3>Security Notes:</h3>
 * <ul>
 *   <li>CORS is restricted to specific origins (not wildcard)</li>
 *   <li>Authentication required for all WebSocket connections</li>
 *   <li>Authorization checked per-message in WebSocketController</li>
 * </ul>
 * 
 * <h3>Future Consideration:</h3>
 * For stricter isolation, could implement per-user topics: /topic/test-results/{userId}
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/submissions")
                .setAllowedOriginPatterns(
                        "http://localhost:4200",
                        "http://localhost:8080",
                        "http://localhost:8095"
                )
                .withSockJS();
    }

}
