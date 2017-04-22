package com.richodemus.reader.common.chronicler_adapter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.kittinunf.fuel.httpPost
import com.richodemus.reader.events.ChangePassword
import com.richodemus.reader.events.CreateUser
import com.richodemus.reader.events.Event
import com.richodemus.reader.events.EventType
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import org.glassfish.jersey.media.sse.EventInput
import org.glassfish.jersey.media.sse.SseFeature
import org.slf4j.LoggerFactory
import javax.ws.rs.client.ClientBuilder


class ChroniclerAdapter : com.richodemus.reader.user_service.EventStore, com.richo.reader.subscription_service.EventStore {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = ObjectMapper()
    private val replaySubject = ReplaySubject.create<Event>()
    private val eventInput = ClientBuilder.newBuilder()
            .register(SseFeature::class.java)
            .build()
            .target("http://localhost:8090/api/event-stream/")
            .request()
            .get(EventInput::class.java)

    init {
        mapper.registerModule(KotlinModule())
        Thread(Runnable {
            while (!eventInput.isClosed) {
                val inboundEvent = eventInput.read() ?: break// connection has been closed

                val data = inboundEvent.readData(String::class.java)
                val event = when (figureOutType(data)) {
                    EventType.CREATE_USER -> mapper.readValue(data, CreateUser::class.java)
                    EventType.CHANGE_PASSWORD -> mapper.readValue(data, ChangePassword::class.java)
                }
                replaySubject.onNext(event)
            }
        }).start()
    }

    private fun figureOutType(eventString: String): EventType {
        if (eventString.contains("CREATE_USER")) {
            return EventType.CREATE_USER
        }
        if (eventString.contains("CHANGE_PASSWORD")) {
            return EventType.CHANGE_PASSWORD
        }
        throw IllegalStateException("Can't parse $eventString")
    }

    override fun add(event: Event) {
        val data = mapper.writeValueAsString(event)
        val chroniclerData = mapper.writeValueAsString(ChroniclerEvent(event.eventId.value.toString(), event.type, data))
        val (request, response, result) = "http://localhost:8090/api/events/".httpPost().body(chroniclerData).header(Pair("Content-Type", "application/json")).responseString()
        if (response.httpStatusCode != 200) {
            logger.error("Failed to post event {}", event)
            throw RuntimeException("Failed to post event " + event)
        }
    }

    override fun observe(): Observable<Event> = replaySubject
}
