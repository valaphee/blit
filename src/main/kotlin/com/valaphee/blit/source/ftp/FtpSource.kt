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

package com.valaphee.blit.source.ftp

import com.valaphee.blit.source.Source
import io.ktor.utils.io.pool.DefaultPool
import io.ktor.utils.io.pool.useInstance
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile

/**
 * @author Kevin Ludwig
 */
class FtpSource(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val connectionPoolSize: Int
) : Source<FtpEntry> {
    internal val semaphore = Semaphore(connectionPoolSize)
    internal val pool = object : DefaultPool<FTPClient>(connectionPoolSize) {
        override fun produceInstance() = FTPClient().apply {
            connect(host, port)
            login(username, password)
        }

        override fun clearInstance(instance: FTPClient) = if (instance.isConnected) instance else {
            disposeInstance(instance)
            produceInstance()
        }

        override fun disposeInstance(instance: FTPClient) {
            instance.disconnect()
        }
    }

    override val home: String
        get() = runBlocking {
            semaphore.withPermit {
                pool.useInstance {
                    it.pwd()
                    it.replyString
                }
            }
        }

    override suspend fun get(path: String) = semaphore.withPermit { pool.useInstance { FtpEntry(this, path, FTPFile()) } }

    override fun close() {
        pool.close()
    }
}
