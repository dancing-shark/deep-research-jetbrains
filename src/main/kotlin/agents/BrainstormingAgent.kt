package com.example.agents

import ai.koog.agents.core.agent.AIAgentService
import ai.koog.agents.core.agent.GraphAIAgentService
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.opentelemetry.attribute.CustomAttribute
import ai.koog.agents.features.opentelemetry.feature.OpenTelemetry
import ai.koog.agents.features.opentelemetry.integration.langfuse.addLangfuseExporter
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor

class BrainstormingAgent(
    private val cfg: BaseAgentConfig,
    private val toolRegistry: ToolRegistry
): BaseAgent {
    override val agentName = "BrainstormingAgent"
    override val agentDescription = "A brainstorming agent to generate research keywords."
    override val inputDescription = "Research topic for keyword brainstorming."

    override fun build(): GraphAIAgentService<String, String> =
        AIAgentService(
            promptExecutor = simpleOpenAIExecutor(cfg.openAiApiKey),
            llmModel = OpenAIModels.Chat.GPT5,
            systemPrompt =
            """You are a $agentName specialist.
                    | Take each Keyword, search, create research library item, make keyword as done, optional create or update notes, optional create new keywords.
                    | Do it until all keywords are done.
                    | Keep the research topic in mind.
                    |""".trimMargin(),
            toolRegistry = toolRegistry
        ) {
            install(OpenTelemetry) {
                addLangfuseExporter(
                    langfuseUrl = cfg.lfBase,
                    langfusePublicKey = cfg.lfPublic,
                    langfuseSecretKey = cfg.lfSecret,
                    traceAttributes = listOf(
                        CustomAttribute("langfuse.trace.tags", listOf(agentName))
                    ))
                setVerbose(true)
            }
        }

    override fun getAgentName(): String {
        return agentName
    }

    override fun getAgentDescription(): String {
        return agentDescription
    }

    override fun getInputDescription(): String {
        return inputDescription
    }
}