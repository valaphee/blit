/*
 * Copyright (c) 2021-2022, Valaphee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.valaphee.blit.source

import javafx.scene.control.TreeItem

/**
 * @author Kevin Ludwig
 */
abstract class AbstractEntry<T : Entry<T>> : Entry<T> {
    override val item: TreeItem<Entry<T>> = TreeItem(this)

    override val self get() = this

    override val name get() = path.removeSuffix("/").split('/').last()

    override fun toString() = name
}
