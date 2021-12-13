/*
 * Copyright (c) 2021, Valaphee.
 * All rights reserved.
 */

package com.valaphee.tead.explorer

import javafx.scene.control.TreeItem

/**
 * @author Kevin Ludwig
 */
interface Entry<T : Entry<T>> {
    val item: TreeItem<Entry<T>>

    val name: String
    val size: Long
    val children: Iterable<T>

    fun update()
}
