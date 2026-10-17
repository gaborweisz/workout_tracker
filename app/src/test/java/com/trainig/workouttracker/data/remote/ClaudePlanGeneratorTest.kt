package com.trainig.workouttracker.data.remote

import com.anthropic.core.JsonValue
import com.anthropic.core.http.Headers
import com.anthropic.errors.BadRequestException
import com.trainig.workouttracker.core.domain.model.PlanPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaudePlanGeneratorTest {

    private val generator = ClaudePlanGenerator()

    @Test
    fun `request carries the structured output schema and fallbacks`() {
        val params = generator.buildParams(PlanPreferences(notes = "bad knee"))

        val schema = params.outputConfig().get().format().get().schema().toString()
        listOf("name", "exercises", "sets", "reps", "weightKg", "restSeconds", "notes").forEach { field ->
            assertTrue("Schema is missing $field: $schema", schema.contains(field))
        }
        assertEquals("claude-opus-5-5", params.model().asString())
        assertTrue(params.messages().first().content().toString().contains("bad knee"))
        assertTrue(params._additionalBodyProperties().containsKey("fallbacks"))
    }

    @Test
    fun `fallbacks can be left out of the request`() {
        val params = generator.buildParams(PlanPreferences(), withFallbacks = false)

        assertTrue(params._additionalBodyProperties().isEmpty())
        assertTrue(params._additionalHeaders().values("anthropic-beta").isEmpty())
    }

    @Test
    fun `extracts the human readable message from an API error`() {
        val error = BadRequestException.builder()
            .headers(Headers.builder().build())
            .body(
                JsonValue.from(
                    mapOf(
                        "type" to "error",
                        "error" to mapOf(
                            "type" to "invalid_request_error",
                            "message" to "Your credit balance is too low to access the Anthropic API."
                        )
                    )
                )
            )
            .build()

        assertEquals("Your credit balance is too low to access the Anthropic API.", generator.apiErrorMessage(error))
    }

    @Test
    fun `parses and clamps the model's JSON`() {
        val json = """
            {"name": "Push Day Power", "exercises": [
              {"name": "Bench Press", "sets": 4, "reps": 6, "weightKg": 60.5, "restSeconds": 150, "notes": "Pause on chest"},
              {"name": "  ", "sets": 3, "reps": 10, "weightKg": 0, "restSeconds": 60, "notes": ""},
              {"name": "Push-up", "sets": 99, "reps": 15, "weightKg": -5, "restSeconds": 60, "notes": ""}
            ]}
        """.trimIndent()

        val plan = generator.parsePlan(json)

        assertEquals("Push Day Power", plan.name)
        assertEquals(listOf("Bench Press", "Push-up"), plan.exercises.map { it.name })
        assertEquals(60.5, plan.exercises[0].weightKg, 0.0)
        assertEquals(10, plan.exercises[1].sets)
        assertEquals(0.0, plan.exercises[1].weightKg, 0.0)
    }
}
