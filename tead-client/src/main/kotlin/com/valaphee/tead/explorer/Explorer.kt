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

package com.valaphee.tead.explorer

import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style
import org.apache.sshd.client.SshClient
import org.apache.sshd.sftp.client.impl.DefaultSftpClientFactory
import tornadofx.View
import tornadofx.splitpane
import java.io.File

/**
 * @author Kevin Ludwig
 */
class Explorer : View("Explorer") {
    override val root = splitpane {
        JMetro(this, Style.DARK)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        add(TreePane(LocalEntry(File("."))))
        val ssh = SshClient.setUpDefaultClient()
        ssh.start()
        val sshSession = ssh.connect("test", "162.55.41.109", 22).verify(30000).session
        sshSession.addPasswordIdentity("b3BlbnNzaC1rZXkt")
        sshSession.auth().verify(30000)
        add(TreePane(SshEntry(DefaultSftpClientFactory.INSTANCE.createSftpClient(sshSession), SshEntry.SshPath("/", ""))))
    }
}
