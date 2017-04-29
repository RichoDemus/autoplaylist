package com.richodemus.reader.common.chronicler_adapter

import com.github.kittinunf.fuel.httpPost
import com.richodemus.reader.events.Event
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import org.glassfish.jersey.media.sse.EventInput
import org.glassfish.jersey.media.sse.SseFeature
import org.slf4j.LoggerFactory
import javax.ws.rs.client.ClientBuilder


class ChroniclerAdapter : com.richodemus.reader.user_service.EventStore, com.richo.reader.subscription_service.EventStore, com.richodemus.reader.label_service.EventStore {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val hostname = System.getProperty("CHRONICLER_HOST", "chronicler")
    private val port = System.getProperty("CHRONICLER_PORT", "8080")
    private val replaySubject = ReplaySubject.create<Event>()

    private val eventInput = ClientBuilder.newBuilder()
            .register(SseFeature::class.java)
            .build()
            .target("http://$hostname:$port/api/event-stream/")
            .request()
            .get(EventInput::class.java)

    init {
        Thread(Runnable {
            while (!eventInput.isClosed) {
                val inboundEvent = eventInput.read() ?: break

                val eventString = inboundEvent.readData(String::class.java)
                val event = eventString.toEvent()

                replaySubject.onNext(event)
            }
        }).start()
    }



    override fun add(event: Event) {
        val data = mapper.writeValueAsString(event)
        val chroniclerData = mapper.writeValueAsString(ChroniclerEvent(event.eventId.value.toString(), event.type, data))
        logger.info("Posting event {}", chroniclerData)
        val (request, response, result) = "http://$hostname:$port/api/events/".httpPost().body(chroniclerData).header(Pair("Content-Type", "application/json")).responseString()
        if (response.httpStatusCode != 200) {
            logger.error("Failed to post event {}, {}", event, result)
            throw RuntimeException("Failed to post event $event, $result")
        }
    }

    override fun observe(): Observable<Event> = replaySubject
}
