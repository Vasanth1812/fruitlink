package com.fruitlink.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology configuration.
 * Declares the main topic exchange and all queues/bindings used by FruitLink.
 *
 * Phase 2: Remove the comment from @Configuration below to enable after demo.
 * Also uncomment the RabbitMQ section in application.properties and remove
 * RabbitAutoConfiguration from the spring.autoconfigure.exclude list.
 */
// @Configuration  // <-- Phase 2: uncomment this to enable RabbitMQ
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queues.notifications}")
    private String notificationsQueue;

    @Value("${app.rabbitmq.queues.purchase-orders}")
    private String purchaseOrdersQueue;

    @Value("${app.rabbitmq.queues.delivery-events}")
    private String deliveryEventsQueue;

    // ──────────────────────────── Exchange ────────────────────────────

    @Bean
    public TopicExchange fruitlinkExchange() {
        return ExchangeBuilder.topicExchange(exchange).durable(true).build();
    }

    // ──────────────────────────── Queues ──────────────────────────────

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(notificationsQueue).build();
    }

    @Bean
    public Queue purchaseOrdersQueue() {
        return QueueBuilder.durable(purchaseOrdersQueue).build();
    }

    @Bean
    public Queue deliveryEventsQueue() {
        return QueueBuilder.durable(deliveryEventsQueue).build();
    }

    // ──────────────────────────── Bindings ────────────────────────────

    @Bean
    public Binding notificationsBinding() {
        return BindingBuilder.bind(notificationsQueue())
                .to(fruitlinkExchange())
                .with("notifications.#");
    }

    @Bean
    public Binding purchaseOrdersBinding() {
        return BindingBuilder.bind(purchaseOrdersQueue())
                .to(fruitlinkExchange())
                .with("purchase-orders.#");
    }

    @Bean
    public Binding deliveryEventsBinding() {
        return BindingBuilder.bind(deliveryEventsQueue())
                .to(fruitlinkExchange())
                .with("delivery.#");
    }

    // ──────────────────────────── Converter ───────────────────────────

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}
