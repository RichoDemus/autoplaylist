package com.richodemus.reader.common.kafka_adapter

import com.richodemus.reader.events_v2.Event
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
import kotlin.concurrent.thread

/**
 * Helper class to consume messages from a topic
 */
class KafkaAdapter : EventStore {
    private val logger = LoggerFactory.getLogger(javaClass.name)
    private val topic = "events_v2"

    private val consumer: KafkaConsumer<String, Event>
    private val producer: KafkaProducer<String, Event>

    init {
        val propOrEmpty: String? = System.getProperty("reader.kafka.host", "")
        val kafkaHost = if (propOrEmpty.isNullOrBlank()) "kafka" else propOrEmpty
        val kafkaServer = "$kafkaHost:9092"

        val producerProperties = Properties()
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer)
        producerProperties.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer-${UUID.randomUUID()}")
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer().javaClass)
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EventSerializer().javaClass)

        producer = KafkaProducer(producerProperties)

        val consumerProperties = Properties()
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer)
        consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "KafkaExampleConsumer-${UUID.randomUUID()}")
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer().javaClass)
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventDeserializer().javaClass)
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString())
        consumer = KafkaConsumer(consumerProperties)
        Runtime.getRuntime().addShutdownHook(Thread { close() })
    }

    override fun consume(messageListener: (Event) -> Unit) {
        consumer.subscribe(listOf(topic))
        thread(name = "KafkaConsumer") {
            try {
                while (true) {
                    val records = consumer.poll(Long.MAX_VALUE)

                    if (records.isEmpty) {
                        logger.debug("No messages...")
                        continue
                    }

                    records.map {
                        logger.debug("Received: {}: {}", it.key(), it.value())
                        it.value()
                    }
                            .forEach { messageListener.invoke(it) }
                }
            } catch (e: WakeupException) {
                logger.info("consumer.wakeup() called, Kafka consumer shutting down")
            } finally {
                consumer.close()
            }
        }
    }

    override fun produce(event: Event) {
        // todo create EventId serde instead of using String
        val record: ProducerRecord<String, Event> = ProducerRecord(topic, event.id().value.toString(), event)

        val recordMetadata = producer.send(record).get()
        logger.debug(recordMetadata.toString())
    }

    override fun close() {
        logger.info("Closing producer and consumer...")
        producer.flush()
        producer.close()
        consumer.wakeup()
    }
}
