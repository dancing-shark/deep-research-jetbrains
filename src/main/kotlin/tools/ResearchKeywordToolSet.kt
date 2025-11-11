package com.example.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

@LLMDescription("Tools to manage the research keywords")
class ResearchKeywordToolSet: ToolSet {

    @Serializable
    enum class ResearchStatus { NEW, IN_PROGRESS, DONE, DISCARDED }
    @Serializable
    data class KeywordItem(
        val id: String,
        val keyword: String,
        val description: String,
        val createdAt: Instant = Clock.System.now(),
        val researchStatus: ResearchStatus = ResearchStatus.NEW
    )

    private val store = ConcurrentHashMap<String, KeywordItem>()

    @Tool
    @LLMDescription("Add a new keyword with a description.")
    fun addKeyword(
        @LLMDescription("The keyword term, e.g 'retrieval augmentation generation'.")
        keyword: String,
        @LLMDescription("A brief reason why this keyword is important for your research.")
        description: String): String {
        // TODO: add check for duplicate keywords
        val id = "kw-${store.size + 1}"
        val item = KeywordItem(id, keyword, description)
        store[id] = item
        return "OK: created id=$id keyword=$keyword"
    }

    @Tool
    @LLMDescription("Get a list of all keywords.")
    fun listKeywords(): List<KeywordItem> = store.values.toList()
    // TODO: add a search function

    @Tool
    @LLMDescription("Mark a keyword as done.")
    fun markKeywordAsDone(
        @LLMDescription("The id of the keyword to mark as done.")
        id: String): String {
        val item = store[id] ?: return "ERROR: keyword with id=$id not found"
        store[id] = item.copy(researchStatus = ResearchStatus.DONE)
        return "OK: marked id=$id as done"
    }


}