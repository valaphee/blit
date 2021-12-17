/*
 * MIT License
 *
 * Copyright (c) 2021, Valaphee.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.valaphee.blit.data.manifest

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.inject.Singleton
import com.valaphee.blit.data.Data
import com.valaphee.blit.data.DataType
import javafx.scene.image.Image

/**
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
