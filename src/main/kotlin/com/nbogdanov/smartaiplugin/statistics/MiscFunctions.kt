package com.nbogdanov.smartaiplugin.statistics

import com.intellij.openapi.diagnostic.Logger

fun Logger.debug(f: () -> String) {
    if (this.isDebugEnabled) this.debug(f.invoke())
}

fun Logger.info(f: () -> String) {
    this.info(f.invoke())
}

fun Logger.warn(f: () -> String) {
    this.warn(f.invoke())
}

fun Logger.warn(ex: Throwable, f: () -> String) {
    if (this.isDebugEnabled) this.warn(f.invoke(), ex)
}

fun Logger.error(ex: Throwable, f: () -> String) {
    this.error(f.invoke(), ex)
}