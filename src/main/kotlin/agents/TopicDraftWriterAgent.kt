package com.example.agents

import ai.koog.agents.core.agent.AIAgentService
import ai.koog.agents.core.agent.GraphAIAgentService
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.opentelemetry.attribute.CustomAttribute
import ai.koog.agents.features.opentelemetry.feature.OpenTelemetry
import ai.koog.agents.features.opentelemetry.integration.langfuse.addLangfuseExporter
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor

class TopicDraftWriterAgent(
    cfg: BaseAgentConfig,
    toolRegistry: ToolRegistry
) : BaseAgent(cfg, toolRegistry) {
    override val agentName = "topicDraftWriter"
    override val agentDescription = "Draft a topic"
    override val inputDescription = "Objective for topic drafting"

    override fun build(): GraphAIAgentService<String, String> =
        AIAgentService(
            promptExecutor = simpleOpenAIExecutor(cfg.openAiApiKey),
            llmModel = OpenAIModels.Chat.GPT5,
            systemPrompt =
                """You are a $agentName specialist.
                    | Create a detailed plan to achieve the given objective. Create Topic Plan.
                    | Keep the research topic in mind.
                    |""".trimMargin(),
            toolRegistry = toolRegistry
        ) {
            install(OpenTelemetry.Feature) {
                addLangfuseExporter(
                    langfuseUrl = cfg.lfBase,
                    langfusePublicKey = cfg.lfPublic,
                    langfuseSecretKey = cfg.lfSecret,
                    traceAttributes = listOf(
                        CustomAttribute("langfuse.trace.tags", listOf(agentName))
                    )
                )
                setVerbose(true)
            }
        }


}