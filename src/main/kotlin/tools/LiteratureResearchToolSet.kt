package com.example.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

@LLMDescription("Tools to manage the literature research")
class LiteratureResearchToolSet(
    private val tavilyApiKey: String,
) : ToolSet {

    @Serializable
    data class NoteItem(
        val id: String,
        val title: String,
        val content: String,
        val keywords: List<String>,
    )

    @Serializable
    data class ResearchLibraryItem(
        val id: String,
        val title: String,
        val keywordItemId: String,
        val url: String,
    )

    @Serializable
    private data class TavilyBody(
        val query: String,
        val auto_parameters: Boolean = false,
        val topic: String = "general",
        val search_depth: String = "basic",
        val chunks_per_source: Int = 3,
        val max_results: Int = 5,
        val time_range: String? = null,
        val start_date: String? = "2025-02-09",
        val end_date: String? = "2025-12-29",
        val include_answer: Boolean = true,
        val include_raw_content: Boolean = true,
        val include_images: Boolean = false,
        val include_image_descriptions: Boolean = false,
        val include_favicon: Boolean = false,
        val include_domains: List<String> = emptyList(),
        val exclude_domains: List<String> = emptyList(),
        val country: String? = null
    )

    private val notesStore = ConcurrentHashMap<String, NoteItem>()
    private val libraryStore = ConcurrentHashMap<String, ResearchLibraryItem>()

    private val tavilyClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    explicitNulls = false
                }
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 60_000
        }
    }

    @Tool
    @LLMDescription("Add a new note for interesting things to remember for later writing process")
    fun addNote(
        @LLMDescription("The title of the note.") title: String,
        @LLMDescription("The content of the note.") content: String,
        @LLMDescription("A list of keywords to associate with this note.") keywords: List<String>
    ): String {
        val id = "note-${notesStore.size + 1}"
        notesStore[id] = NoteItem(id, title, content, keywords)
        return "OK: created note with id=$id title=$title"
    }

    @Tool
    @LLMDescription("Edit an existing note with a new title and content")
    fun editNote(id: String, title: String, content: String, keywords: List<String>): String {
        val item = notesStore[id] ?: return "ERROR: note with id=$id not found"
        notesStore[id] = item.copy(title = title, content = content, keywords = keywords)
        return "OK: edited note with id=$id"
    }

    @Tool
    @LLMDescription("Delete a note by its id")
    fun deleteNote(id: String): String {
        notesStore.remove(id)
        return "OK: deleted note with id=$id"
    }

    @Tool
    @LLMDescription("Get list of all notes")
    fun listNotes(): List<NoteItem> = notesStore.values.toList()

    @Tool
    @LLMDescription("Search the web with Tavily (returns raw JSON).")
    suspend fun search_web(
        @LLMDescription("The query to search on the web.") query: String
    ): String {
        val response: HttpResponse = tavilyClient.request {
            method = HttpMethod.Post
            // -> Kein DefaultRequest mehr: volle URL setzen
            url("https://api.tavily.com/search")
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $tavilyApiKey")
            setBody(TavilyBody(query = query))
        }
        return response.bodyAsText()
    }

    @Tool
    @LLMDescription("Add a new research library item.")
    fun addLibraryItem(
        @LLMDescription("The title of the research library item.") title: String,
        @LLMDescription("The keyword item id to associate with this research library item.") keywordItemId: String,
        @LLMDescription("The URL of the research library item.") url: String
    ): String {
        val id = "lib-${libraryStore.size + 1}"
        libraryStore[id] = ResearchLibraryItem(id, title, keywordItemId, url)
        return "OK: created library item with id=$id title=$title"
    }
}
