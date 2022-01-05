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

package com.valaphee.blit.data.manifest

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.inject.Singleton
import com.valaphee.blit.data.Data
import com.valaphee.blit.data.DataType
import javafx.scene.image.Image

/**
 * Icon manifest data
 *
 * @author Kevin Ludwig
 */
@Singleton
@DataType("icon_manifest")
class IconManifest(
    @get:JsonProperty("default_file_icon") val defaultFileIcon: FileIcon,
    @get:JsonProperty("file_icons") val fileIcons: List<FileIcon>,
    @get:JsonProperty("default_folder_icon") val defaultFolderIcon: FolderIcon,
    @get:JsonProperty("folder_icons") val folderIcons: List<FolderIcon>
) : Data {
    class FileIcon(
        @get:JsonProperty("name") val name: String,
        @get:JsonProperty("file_names") val fileNames: List<String> = emptyList(),
        @get:JsonProperty("file_extensions") val fileExtensions: List<String> = emptyList()
    ) {
        @get:JsonIgnore val image by lazy { Image(IconManifest::class.java.getResourceAsStream("/icon/$name.svg"), 16.0, 16.0, false, false) }
    }

    class FolderIcon(
        @get:JsonProperty("name") val name: String,
        @get:JsonProperty("folder_names") val folderNames: List<String> = emptyList()
    ) {
        @get:JsonIgnore val image by lazy { Image(IconManifest::class.java.getResourceAsStream("/icon/$name.svg"), 16.0, 16.0, false, false) }
    }
}
