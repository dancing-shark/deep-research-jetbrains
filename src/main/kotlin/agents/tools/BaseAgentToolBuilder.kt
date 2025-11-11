package com.example.agents.tools

import ai.koog.agents.core.agent.AIAgentTool
import ai.koog.agents.core.agent.createAgentTool
import ai.koog.agents.core.tools.Tool
import com.example.agents.BaseAgent

class BaseAgentToolBuilder (
    private val agent: BaseAgent
) {
    fun build(): Tool<AIAgentTool.AgentToolArgs, AIAgentTool.AgentToolResult> {
        val service = agent.build()
        return service.createAgentTool(
            agentName = agent.agentName,
            agentDescription = agent.agentDescription,
            inputDescription = agent.inputDescription
        )
    }
}