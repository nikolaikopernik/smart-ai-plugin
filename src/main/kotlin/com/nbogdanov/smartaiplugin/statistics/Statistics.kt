package com.nbogdanov.smartaiplugin.statistics

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.eventLog.events.EventId1
import com.intellij.internal.statistic.eventLog.events.EventId2
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import com.intellij.lang.Language

enum class Inspection { dummy_names, complexity }
enum class CommunicationIssues { io_exception, http_4xx, http_5xx, timeout, other }
enum class Lang { java, kotlin, other }

/**
 * Initially based on Feature usage collector functionality.
 * But didn't figure out on how to collect those metics (looks like an internal feature of Intellij) also
 * almost no documentation, so made a backup implementation with map and a local file in the project
 */
object Statistics : CounterUsagesCollector() {
    private val GROUP = EventLogGroup("com.nbogdanov.smartaiplugin.statistics", 1)

    //@formatter:off
    private val EVENT_INSPECTION_TRIGGERED = GROUP.registerEvent("triggered", EventFields.Enum<Inspection>("inspection"))
    private val EVENT_NETWORK_ISSUE = GROUP.registerEvent("network-issue", EventFields.Enum<Inspection>("inspection"), EventFields.Enum<CommunicationIssues>("issue"))
    private val EVENT_MODEL_NOT_FINISHED = GROUP.registerEvent("model-not-finished", EventFields.Enum<Inspection>("inspection"))
    private val EVENT_MODEL_JSON_ISSUE = GROUP.registerEvent("model-incorrect-json", EventFields.Enum<Inspection>("inspection"))
    private val EVENT_CANNOT_LOCATE_CODE = GROUP.registerEvent("cannot-locate-code", EventFields.Enum<Inspection>("inspection"), EventFields.Enum<Lang>("lang"))
    private val EVENT_NO_PROBLEMS_TO_SHOW = GROUP.registerEvent("no-problems-to-show", EventFields.Enum<Inspection>("inspection"))
    private val EVENT_FIX_SHOWN = GROUP.registerEvent("fix-shown", EventFields.Enum<Inspection>("inspection"))
    private val EVENT_REFACTOR_FAILED = GROUP.registerEvent("fix-shown-refactor-failed", EventFields.Enum<Inspection>("inspection"))
    private val EVENT_REFACTOR_CANCELLED = GROUP.registerEvent("fix-shown-refactor-cancelled", EventFields.Enum<Inspection>("inspection"))
    private val EVENT_FIX_APPLIED = GROUP.registerEvent("fix-applied", EventFields.Enum<Inspection>("inspection"))
    //@formatter:on

    override fun getGroup(): EventLogGroup = GROUP

    fun logInspectionStarted(inspection: Inspection) {
        EVENT_INSPECTION_TRIGGERED.increment(inspection)
    }

    fun logNetworkIssue(inspection: Inspection, issue: CommunicationIssues) {
        EVENT_NETWORK_ISSUE.increment(inspection, issue)
    }

    fun logModelNotFinished(inspection: Inspection) {
        EVENT_MODEL_NOT_FINISHED.increment(inspection)
    }

    fun logModelJsonIssue(inspection: Inspection) {
        EVENT_MODEL_JSON_ISSUE.increment(inspection)
    }

    fun logCannotLocateProblemCode(inspection: Inspection, lang: Lang) {
        EVENT_CANNOT_LOCATE_CODE.increment(inspection, lang)
    }

    fun logFixShown(inspection: Inspection) {
        EVENT_FIX_SHOWN.increment(inspection)
    }

    fun logFixApplied(inspection: Inspection) {
        EVENT_FIX_APPLIED.increment(inspection)
    }

    fun logFixShownRefactorFailed() {
        EVENT_REFACTOR_FAILED.increment(Inspection.complexity)
    }

    fun logFixShownRefactorCancelled() {
        EVENT_REFACTOR_CANCELLED.increment(Inspection.complexity)
    }

    fun logNoProblems(inspection: Inspection) {
        EVENT_NO_PROBLEMS_TO_SHOW.increment(inspection)
    }


    private fun <T> EventId1<T>.increment(a: T) {
        this.log(a)
        LocalStatistics.getInstance().record(this.eventId, a)
    }

    private fun <T, K> EventId2<T, K>.increment(a: T, b: K) {
        this.log(a, b)
        LocalStatistics.getInstance().record(this.eventId, a, b)
    }
}


fun Language.lang() = when (this.id.lowercase()) {
    "kotlin" -> Lang.kotlin
    "java" -> Lang.java
    else -> Lang.other
}