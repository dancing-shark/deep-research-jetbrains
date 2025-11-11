package com.example.agents

import ai.koog.agents.core.agent.GraphAIAgentService
import ai.koog.agents.core.tools.ToolRegistry

abstract class BaseAgent(
    protected val cfg: BaseAgentConfig,
    protected val toolRegistry: ToolRegistry,
) {
    abstract val agentName: String   // public read-only
    abstract val agentDescription: String
    abstract val inputDescription: String

    abstract fun build(): GraphAIAgentService<String, String>
}