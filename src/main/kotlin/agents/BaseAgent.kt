package com.example.agents

import ai.koog.agents.core.agent.GraphAIAgentService

interface BaseAgent {
    val agentName: String
    val agentDescription: String
    val inputDescription: String

    fun build(): GraphAIAgentService<String, String>

    fun getAgentName(): String
    fun getAgentDescription(): String
    fun getInputDescription(): String
}