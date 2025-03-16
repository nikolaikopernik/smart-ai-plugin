package com.nbogdanov.smartaiplugin.statistics

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.jetbrains.rd.util.concurrentMapOf

/**
 * Storing our metrics in a local file
 * From this storage we can later send it to our system.
 * But I believe there is already a built in system for that in Intellij
 */
@Service(Service.Level.APP)
@State(
    name = "DummyAIStatistics",
    storages = [Storage("dummy-ai-statistics.xml")]
)
class LocalStatistics : PersistentStateComponent<LocalStatistics.State> {
    private var state = concurrentMapOf<String, Int>()

    /**
     * Record a metric
     */
    fun record(vararg features: Any?) {
        val key = features.joinToString(".") { it.toString() }.lowercase()
        state.compute(key) { _, v ->
            v?.plus(1) ?: 1
        }
    }

    override fun getState(): LocalStatistics.State {
        return State(this.state)
    }

    override fun loadState(state: LocalStatistics.State) {
        this.state = state.dataMap
    }

    companion object {
        fun getInstance(): LocalStatistics {
            return ApplicationManager.getApplication().getService(LocalStatistics::class.java)
        }
    }

    data class State(
        var dataMap: MutableMap<String, Int> = mutableMapOf()  // Store a map
    )
}
