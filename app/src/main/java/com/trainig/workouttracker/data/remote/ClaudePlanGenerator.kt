package com.trainig.workouttracker.data.remote

import android.util.Log
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.core.JsonValue
import com.anthropic.core.jsonMapper
import com.anthropic.errors.AnthropicIoException
import com.anthropic.errors.AnthropicServiceException
import com.anthropic.errors.BadRequestException
import com.anthropic.errors.PermissionDeniedException
import com.anthropic.errors.RateLimitException
import com.anthropic.errors.UnauthorizedException
import com.anthropic.models.messages.JsonOutputFormat
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.OutputConfig
import com.anthropic.models.messages.StopReason
import com.fasterxml.jackson.databind.JsonNode
import com.trainig.workouttracker.core.domain.model.Equipment
import com.trainig.workouttracker.core.domain.model.PlanExercise
import com.trainig.workouttracker.core.domain.model.PlanPreferences
import com.trainig.workouttracker.core.domain.model.WorkoutPlan
import com.trainig.workouttracker.core.domain.repository.AiPlanGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClaudePlanGenerator @Inject constructor() : AiPlanGenerator {

    override suspend fun generate(preferences: PlanPreferences, apiKey: String): Result<WorkoutPlan> =
        withContext(Dispatchers.IO) {
            val client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build()
            try {
                val message = try {
                    client.messages().create(buildParams(preferences))
                } catch (e: BadRequestException) {
                    // Refusal fallbacks are a beta; if this account can't use them, ask again without
                    val detail = apiErrorMessage(e)
                    if (!detail.contains("fallback", ignoreCase = true) && !detail.contains("beta", ignoreCase = true)) throw e
                    Log.w(TAG, "Retrying without refusal fallbacks: $detail")
                    client.messages().create(buildParams(preferences, withFallbacks = false))
                }
                if (message.stopReason().orElse(null) == StopReason.REFUSAL) {
                    return@withContext Result.failure(
                        AiPlanException("Claude declined to create this plan. Try rewording your notes.")
                    )
                }

                val json = message.content().firstNotNullOfOrNull { block -> block.text().orElse(null)?.text() }
                    ?: return@withContext Result.failure(AiPlanException("Claude returned an empty plan. Please try again."))

                val plan = parsePlan(json)
                if (plan.exercises.isEmpty()) {
                    Result.failure(AiPlanException("Claude returned a plan without exercises. Please try again."))
                } else {
                    Result.success(plan)
                }
            } catch (e: UnauthorizedException) {
                Result.failure(AiPlanException("Your Anthropic API key was rejected. Check it in Settings.", e))
            } catch (e: PermissionDeniedException) {
                Result.failure(AiPlanException("This API key doesn't have access to $MODEL.", e))
            } catch (e: RateLimitException) {
                Result.failure(AiPlanException("Too many requests right now. Wait a minute and try again.", e))
            } catch (e: AnthropicServiceException) {
                // Show Anthropic's own explanation (e.g. "Your credit balance is too low…"); the status code alone isn't actionable
                val detail = apiErrorMessage(e).ifBlank { "Please try again." }
                Result.failure(AiPlanException("Claude API error (${e.statusCode()}): $detail", e))
            } catch (e: AnthropicIoException) {
                // Android keeps blocking sockets for an app whose update newly added INTERNET until the device restarts
                val blockedByOs = generateSequence<Throwable>(e) { it.cause }.any { it.message?.contains("EPERM") == true }
                val message = if (blockedByOs) {
                    "Network access isn't active yet after the update. Restart your phone once, then try again."
                } else {
                    "Couldn't reach Claude. Check your internet connection."
                }
                Result.failure(AiPlanException(message, e))
            } catch (e: Exception) {
                Result.failure(AiPlanException("Couldn't read Claude's plan. Please try again.", e))
            } finally {
                client.close()
            }.onFailure { Log.w(TAG, "AI plan generation failed", it.cause ?: it) }
        }

    internal fun buildParams(preferences: PlanPreferences, withFallbacks: Boolean = true): MessageCreateParams =
        MessageCreateParams.builder()
            .model(MODEL)
            .maxTokens(16000L)
            .system(SYSTEM_PROMPT)
            .addUserMessage(buildPrompt(preferences))
            // Explicit schema: the SDK's class-based schema derivation uses reflection APIs Android lacks
            .outputConfig(
                OutputConfig.builder()
                    .effort(OutputConfig.Effort.MEDIUM)
                    .format(JsonOutputFormat.builder().schema(PLAN_SCHEMA).build())
                    .build()
            )
            .apply {
                if (withFallbacks) {
                    // Retry on a model Anthropic picks if a safety classifier declines the request
                    putAdditionalHeader("anthropic-beta", "server-side-fallback-2026-07-01")
                    putAdditionalBodyProperty("fallbacks", JsonValue.from("default"))
                }
            }
            .build()

    /** The human-readable `error.message` from an API error body, or "" if there isn't one. */
    internal fun apiErrorMessage(e: AnthropicServiceException): String = try {
        val body = jsonMapper().readTree(jsonMapper().writeValueAsString(e.body()))
        body.path("error").path("message").asText().ifBlank { body.path("message").asText() }
    } catch (_: Exception) {
        ""
    }

    private fun buildPrompt(preferences: PlanPreferences): String = buildString {
        appendLine("Create one gym workout session for me.")
        appendLine("Goal: ${preferences.goal.label}")
        appendLine("Focus: ${preferences.focus.label}")
        appendLine("Experience: ${preferences.experience.label}")
        appendLine("Available equipment: ${equipmentDescription(preferences.equipment)}")
        appendLine("Time available: about ${preferences.durationMinutes} minutes including rest")
        if (preferences.notes.isNotBlank()) {
            appendLine("My notes: ${preferences.notes.trim()}")
        }
    }

    private fun equipmentDescription(equipment: Equipment): String = when (equipment) {
        Equipment.FULL_GYM -> "a fully equipped gym (barbells, dumbbells, cables, machines)"
        Equipment.DUMBBELLS -> "dumbbells and a bench only"
        Equipment.BODYWEIGHT -> "bodyweight only (a pull-up bar is available)"
    }

    /** Reads the structured-output JSON; values are clamped so a bad reply can't produce absurd plans. */
    internal fun parsePlan(json: String): WorkoutPlan {
        val root = jsonMapper().readTree(json)
        return WorkoutPlan(
            name = root.path("name").asText().trim().ifBlank { "AI Workout" },
            exercises = root.path("exercises").mapNotNull { it.toPlanExercise() }
        )
    }

    private fun JsonNode.toPlanExercise(): PlanExercise? {
        val name = path("name").asText().trim()
        if (name.isBlank()) return null
        return PlanExercise(
            name = name,
            sets = path("sets").asInt(3).coerceIn(1, 10),
            reps = path("reps").asInt(10).coerceIn(1, 100),
            weightKg = path("weightKg").asDouble(0.0).coerceIn(0.0, 500.0),
            restSeconds = path("restSeconds").asInt(90).coerceIn(0, 600),
            notes = path("notes").asText().trim()
        )
    }

    class AiPlanException(message: String, cause: Throwable? = null) : Exception(message, cause)

    internal companion object {
        const val MODEL = "claude-opus-5-5"
        private const val TAG = "ClaudePlanGenerator"

        val SYSTEM_PROMPT = """
            You are an experienced strength and conditioning coach designing a single gym session.
            Fit the session into the stated time including rest. Put compound lifts before isolation work.
            Choose sets, reps and rest that match the goal and experience level, and only use the equipment available.
            Suggest conservative starting weights in kilograms for an average lifter at the stated level; use 0 for
            bodyweight exercises. Respect any injuries or preferences in the notes.
        """.trimIndent()

        private fun prop(type: String, description: String) =
            mapOf("type" to type, "description" to description)

        private val EXERCISE_SCHEMA = mapOf(
            "type" to "object",
            "properties" to mapOf(
                "name" to prop("string", "Common gym name of the exercise"),
                "sets" to prop("integer", "Number of working sets"),
                "reps" to prop("integer", "Repetitions per set"),
                "weightKg" to prop("number", "Suggested starting weight in kilograms; 0 for bodyweight exercises"),
                "restSeconds" to prop("integer", "Rest between sets in seconds"),
                "notes" to prop("string", "One short technique cue, at most 60 characters")
            ),
            "required" to listOf("name", "sets", "reps", "weightKg", "restSeconds", "notes"),
            "additionalProperties" to false
        )

        val PLAN_SCHEMA: JsonOutputFormat.Schema = JsonOutputFormat.Schema.builder()
            .putAdditionalProperty("type", JsonValue.from("object"))
            .putAdditionalProperty(
                "properties",
                JsonValue.from(
                    mapOf(
                        "name" to prop("string", "Short, motivating name for the workout, e.g. \"Push Day Power\""),
                        "exercises" to mapOf(
                            "type" to "array",
                            "description" to "Exercises in the order they should be performed",
                            "items" to EXERCISE_SCHEMA
                        )
                    )
                )
            )
            .putAdditionalProperty("required", JsonValue.from(listOf("name", "exercises")))
            .putAdditionalProperty("additionalProperties", JsonValue.from(false))
            .build()
    }
}
