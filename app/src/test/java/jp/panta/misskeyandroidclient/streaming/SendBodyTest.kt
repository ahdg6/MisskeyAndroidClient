package jp.panta.misskeyandroidclient.streaming

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class SendBodyTest {

    @Test
    fun testParseMain() {
        val main: Send = Send.Connect(Send.Connect.Body(channel = Send.Connect.Type.HOME_TIMELINE, id = "hoge"))

        val h = Json.encodeToString(main)
        println(h)
        assertTrue(true)
    }

    @Test
    fun testDecode() {
        val json = """{"type":"connect","body":{"id":"hoge","channel":"homeTimeline"}}"""
        val main: Send = Json.decodeFromString(json)
        println(main)
        assertTrue(true)
    }

    @Test
    fun testParseSubNote() {
        val obj: Send = Send.SubscribeNote(Send.SubscribeNote.Body("hogepiyo"))
        val h = Json.encodeToString(obj)

        println(h)
        assertTrue(true)
    }

    @Test
    fun testDecodeSubNote() {
        val json = """{"type":"sn","body":{"id":"hogepiyo"}}"""
        val h: Send = Json.decodeFromString(json)

        println(h)
        assertTrue(h is Send.SubscribeNote)
    }

}