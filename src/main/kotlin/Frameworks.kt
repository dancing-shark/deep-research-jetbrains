package com.example

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.ext.agent.chatAgentStrategy
import ai.koog.agents.features.opentelemetry.feature.OpenTelemetry
import ai.koog.agents.features.opentelemetry.integration.langfuse.addLangfuseExporter
import ai.koog.ktor.Koog
import ai.koog.ktor.aiAgent
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import com.example.agents.BaseAgentConfig
import com.example.agents.BrainstormingAgent
import com.example.agents.LiteratureResearchAgent
import com.example.agents.PlanningAgent
import com.example.agents.RefiningAgent
import com.example.agents.ReviewAgent
import com.example.agents.TopicDraftWriterAgent
import com.example.agents.tools.BaseAgentToolBuilder
import com.example.tools.LiteratureResearchToolSet
import com.example.tools.ResearchKeywordToolSet
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    val cfg = BaseAgentConfig(
        openAiApiKey = environment.config.property("koog.openai.apikey").getString(),
        lfBase = environment.config.property("langfuse.lfbase").getString(),
        lfPublic = environment.config.property("langfuse.lfpublic").getString(),
        lfSecret = environment.config.property("langfuse.lfsecret").getString()
        )

    val reseachKeywordToolSet = ResearchKeywordToolSet()
    val literatureResearchToolSet = LiteratureResearchToolSet(
        environment.config.property("tavily.apikey").getString()
    )


    val brainstormingAgent = BrainstormingAgent(cfg, toolRegistry = ToolRegistry{
        tools(reseachKeywordToolSet)


    })
    val literatureResearchAgent = LiteratureResearchAgent(cfg, toolRegistry = ToolRegistry{
        tools(reseachKeywordToolSet)
        tools(literatureResearchToolSet)
    })
    val planningAgent = PlanningAgent(cfg, toolRegistry = ToolRegistry{
        tools(reseachKeywordToolSet)
        tools(literatureResearchToolSet)
    })
    val refiningAgent = RefiningAgent(cfg, toolRegistry = ToolRegistry{

    })
    val reviewAgent = ReviewAgent(cfg, toolRegistry = ToolRegistry{
        tools(reseachKeywordToolSet)
        tools(literatureResearchToolSet)
    })
    val topicDraftWriterAgent = TopicDraftWriterAgent(cfg, toolRegistry = ToolRegistry{
        tools(reseachKeywordToolSet)
        tools(literatureResearchToolSet)
    })

    val brainstormingAgentTool = BaseAgentToolBuilder(brainstormingAgent).build()
    val literatureResearchAgentTool = BaseAgentToolBuilder(literatureResearchAgent).build()
    val planningAgentTool = BaseAgentToolBuilder(planningAgent).build()
    val refiningAgentTool = BaseAgentToolBuilder(refiningAgent).build()
    val reviewAgentTool = BaseAgentToolBuilder(reviewAgent).build()
    val topicDraftWriterAgentTool = BaseAgentToolBuilder(topicDraftWriterAgent).build()


    install(Koog) {
        llm {
            openAI(apiKey = cfg.openAiApiKey)
        }

        agentConfig {
            prompt("deep-research-agent") {
                system("""
                    You are a research assistant.
                    If not other requested always do does steps:
                    1. brainstormingAgentTool
                    2. literatureResearchAgentTool
                    3. planningAgentTool
                    4. topicDraftWriterAgentTool
                    5. refiningAgentTool
                    6. reviewAgentTool
                """.trimIndent())
            }
            maxAgentIterations = 30

            registerTools {
                tool(brainstormingAgentTool)
                tool(literatureResearchAgentTool)
                tool(planningAgentTool)
                tool(refiningAgentTool)
                tool(reviewAgentTool)
                tool(topicDraftWriterAgentTool)
            }

            install(OpenTelemetry) {
                addLangfuseExporter(
                    langfuseUrl = cfg.lfBase,
                    langfusePublicKey = cfg.lfPublic,
                    langfuseSecretKey = cfg.lfSecret
                )
                setVerbose(true)
            }
        }
    }

    
    routing {
        route("/ai") {
            post("/chat") {
                val userInput = call.receive<String>()
                val output = aiAgent<String, String>(
                    strategy = chatAgentStrategy(),
                    model = OpenAIModels.Chat.GPT4_1,
                    input = userInput
                )


                call.respondText(output)
            }
        }
    }
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<HelloService> {
                HelloService {
                    println(environment.log.info("Hello, World!"))
                }
            }
        })
    }
}
