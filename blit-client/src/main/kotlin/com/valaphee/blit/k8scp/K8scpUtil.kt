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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val spaces = "\\s+".toRegex()
private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS Z")

internal fun K8scpSource.stat(path: String): SftpClient.Attributes? {
    val process = K8scpSource.exec.exec(namespace, pod, arrayOf("stat", "--format", "%A 0 %U %G %s %y %n", path), false)
    val attributes = BufferedReader(InputStreamReader(process.inputStream)).use { parseLsEntry(it.readText())?.second }
    process.waitFor()
    return attributes
}

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
