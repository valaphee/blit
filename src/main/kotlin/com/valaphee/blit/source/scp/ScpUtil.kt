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

package com.valaphee.blit.source.scp

import org.apache.sshd.sftp.client.SftpClient
import org.apache.sshd.sftp.common.SftpConstants
import java.nio.file.attribute.FileTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val spaces = "\\s+".toRegex()
private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS Z")

internal fun parseLsEntry(entry: String): Pair<String, SftpClient.Attributes>? {
    val entryColumns = entry.replace(spaces, " ").trim().split(' ')
    return if (entryColumns.size == 9) entryColumns[8] to SftpClient.Attributes().apply {
        val permission = entryColumns[0]
        permissions = when (permission[0]) {
            '-' -> SftpConstants.S_IFREG
            'd' -> SftpConstants.S_IFDIR
            else -> 0
        }
        owner = entryColumns[2]
        group = entryColumns[3]
        entryColumns[4].toLongOrNull()?.let { size = it }
        modifyTime(FileTime.from(ZonedDateTime.parse("${entryColumns[5]} ${entryColumns[6]} ${entryColumns[7]}", dateTimeFormatter).toInstant()))
    } else null
}
