package com.richodemus.reader.common.kafka_adapter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.richodemus.reader.events.Event
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.util.Properties
import java.util.UUID

/**
 * Helper class to consume messages from a topic
 */
class KafkaAdapter : EventStore {
    private val logger = LoggerFactory.getLogger(javaClass.name)

    private val mapper = jacksonObjectMapper()
    private val consumer: KafkaConsumer<String, String>
    private val producer: KafkaProducer<String, String>
    private var running = true

    init {
        val producerProperties = Properties()
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        producerProperties.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer-${UUID.randomUUID()}")
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)

        producer = KafkaProducer(producerProperties)

        val consumerProperties = Properties()
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "KafkaExampleConsumer-${UUID.randomUUID()}")
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString())
        consumer = KafkaConsumer(consumerProperties)
        Runtime.getRuntime().addShutdownHook(Thread { close() })
    }

    override fun consume(messageListener: (Event) -> Unit) {
        consumer.subscribe(listOf("events"))
        Thread {
            try {
                while (running) {
                    val records = consumer.poll(Long.MAX_VALUE)

                    if (records.isEmpty) {
                        logger.debug("No messages...")
                        continue
                    }

                    records.map {
                        logger.debug("Received: {}: {}", it.key(), it.value())
                        it.value()
                    }
                            .map { mapper.readValue(it, WrapperEvent::class.java) }
                            .map { it.data }
                            .map { it.toEvent() }
                            .forEach { messageListener.invoke(it) }
                }
            } catch (e: WakeupException) {
                logger.info("consumer.wakeup() called, Kafka consumer shutting down")
            } finally {
                consumer.close()
            }
        }.start()
    }

    override fun produce(event: Event) {
        val data = mapper.writeValueAsString(event)
        val chroniclerData = mapper.writeValueAsString(ChroniclerEvent(event.eventId.value.toString(), event.type, data))
        val record: ProducerRecord<String, String> = ProducerRecord("events", event.eventId.toString(), chroniclerData)

        val recordMetadata = producer.send(record).get()
        logger.debug(recordMetadata.toString())
    }

    override fun close() {
        logger.info("Closing producer and consumer...")
        running = false
        producer.flush()
        producer.close()
        consumer.wakeup()
    }
}
