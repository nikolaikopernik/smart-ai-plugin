package com.nbogdanov.smartaiplugin.misc

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalDateTime.now

/**
 * To minimize the load on LLM we can have a cache in between
 * The idea is:
 *  - do not call LLM if the file is not changed
 *  - even if file is changed - update LLM results once in 5 seconds
 */
class AICommunicationCache {
    private val CACHE_KEEP_INTERVAL_SEC = 5L

    private val cache: Cache<String, CachedRequest> = Caffeine.newBuilder()
        .maximumWeight(10_000_000)
        // weigh it as a number of chars in the response (as the most significant object in the cache)
        .weigher {  key:String, value: CachedRequest -> value.response?.toString()?.length ?: 1 }
        .build()


    suspend fun <T> getOrCall(request: AIRequest<T>, valueProvider: suspend (AIRequest<T>) -> T?): T? {
        val key = request.key()
        val item = cache.getIfPresent(key)
        if (item == null) {
            return valueProvider(request).also {
                cache.put(key, request.toCachedRequest(it))
            }
        }
        if(item.calculated.isAfter(now(clock).minusSeconds(CACHE_KEEP_INTERVAL_SEC)) ||
            item.contentHash == request.fileContent().hashCode()) {
            return item.response as T?
        }
        val response = valueProvider(request)
        cache.put(key, request.toCachedRequest(response))
        return response
    }

    private fun AIRequest<*>.toCachedRequest(response: Any?) = CachedRequest(this.filePath(),
        this.javaClass,
        this.fileContent().hashCode(),
        now(clock),
        response)

    // cache key, so we cache a response per file/request type
    private fun AIRequest<*>.key() = this.filePath() + ";" + this.javaClass.name

    companion object {
        // this is only for testing to be able to manipulate time
        private var clock = Clock.systemDefaultZone()

        fun setClockTestOnly(clock: Clock) {
            this.clock = clock
        }
    }
}

data class CachedRequest(val filePath: String,
                         val requestClass: Class<*>,
                         val contentHash: Int,
                         val calculated: LocalDateTime,
                         val response: Any?)


