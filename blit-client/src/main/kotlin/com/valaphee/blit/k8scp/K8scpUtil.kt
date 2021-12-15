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

package com.valaphee.blit.k8scp

import org.apache.sshd.sftp.client.SftpClient
import org.apache.sshd.sftp.common.SftpConstants
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.attribute.FileTime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Calendar
import java.util.Locale

private val spaces = "\\s+".toRegex()
private val dateTimeFormatter = DateTimeFormatterBuilder().appendPattern("MMM d HH:mm").parseDefaulting(ChronoField.YEAR, Calendar.getInstance().get(Calendar.YEAR).toLong()).toFormatter(Locale.ENGLISH)

internal fun K8scpSource.stat(path: String): SftpClient.Attributes? {
    val process = exec.exec(namespace, pod, arrayOf("ls", "-l", path), false)
    val children = BufferedReader(InputStreamReader(process.inputStream)).use { it.readLines().mapNotNull { parseLsEntry(it)?.second } }
    process.waitFor()
    return children.firstOrNull()
}

internal fun parseLsEntry(entry: String): Pair<String, SftpClient.Attributes>? {
    val entryColumns = entry.replace(spaces, " ").split(' ')
    return if (entryColumns.size >= 6) entryColumns[8] to SftpClient.Attributes().apply {
        val permission = entryColumns[0]
        permissions = when (permission[0]) {
            '-' -> SftpConstants.S_IFREG
            'd' -> SftpConstants.S_IFDIR
            else -> 0
        }
        owner = entryColumns[2]
        group = entryColumns[3]
        entryColumns[4].toLongOrNull()?.let { size = it }
        modifyTime(FileTime.from(LocalDateTime.parse("${entryColumns[5]} ${entryColumns[6]} ${entryColumns[7]}", dateTimeFormatter).toInstant(ZoneOffset.UTC)))
    } else null
}
