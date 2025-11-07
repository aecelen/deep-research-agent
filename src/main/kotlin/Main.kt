package org.example

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.singleRunStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import io.github.cdimascio.dotenv.dotenv

suspend fun main() {
    // Load .env file
    val dotenv = dotenv { directory = "." }
    val openAiApiKey = dotenv["OPENAI_API_KEY"]
    val brightDataKey = dotenv["BRIGHT_DATA_API_KEY"]

    println("Deep Research Agent")
    println("=".repeat(80))

    // Get research question from user
    print("What would you like to research? ")
    val userMessage = readln()

    println("Starting research...\n")

    // Register tools
    val webSearchTools = WebSearchTools(brightDataKey)
    val toolRegistry = ToolRegistry {
        tools(webSearchTools)
    }

    // Configure agent
    val executor = simpleOpenAIExecutor(openAiApiKey)
    val maxIterations = 30  // Max tool call iterations the agent can make
    val agentConfig = AIAgentConfig(
        prompt = prompt("deep_research_prompt") {
            system(AgentPrompts.SYSTEM_PROMPT)
        },
        model = OpenAIModels.Chat.GPT4o,
        maxAgentIterations = maxIterations,
    )

    // Track iterations
    var toolCount = 0
    var llmCount = 0

    // Create agent
    val agent = AIAgent(
        promptExecutor = executor,
        strategy = singleRunStrategy(),
        toolRegistry = toolRegistry,
        agentConfig = agentConfig,
    ) {
        handleEvents {
            onLLMCallStarting { ctx ->
                llmCount++
                println("\n[DEBUG] LLM Call #$llmCount - Thinking...")
            }

            onLLMCallCompleted { ctx ->
                println("[DEBUG] LLM Call #$llmCount - Decided.")
            }

            onToolCallStarting { ctx ->
                toolCount++
                println("\n[DEBUG] Tool Call #$toolCount - Executing: ${ctx.tool.name}")
            }

            onToolCallCompleted { ctx ->
                val resultString = ctx.result.toString()
                val previewOfResult = resultString.take(75)
                println("[DEBUG] Result Preview: $previewOfResult...")
            }
        }
    }

    // Run agent
    val result = agent.run(userMessage)

    // Print result
    println("Research is complete with total $toolCount tool calls.")
    println("=".repeat(80))
    println(result)
}