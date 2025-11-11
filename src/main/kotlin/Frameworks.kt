package com.example

import ai.koog.agents.ext.agent.chatAgentStrategy
import ai.koog.ktor.Koog
import ai.koog.ktor.aiAgent
import ai.koog.prompt.executor.clients.openai.OpenAIModels
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
    install(Koog) {
        llm {
            openAI(apiKey = environment.config.property("koog.openai.apikey").getString())
        }

        agentConfig {
            prompt("deep-research-agent") {
                system("""
                    You are a research assistant.
                    If not other requested always do does steps:
                    1. 
                """.trimIndent())
            }
            maxAgentIterations = 30
        }
    }

    
    routing {
        route("/ai") {
            post("/chat") {
                val userInput = call.receive<String>()
                val output = aiAgent<String, String>(
                    strategy = chatAgentStrategy(),
                    model = OpenAIModels.Chat.GPT4_1,
                    input = userInput,
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
