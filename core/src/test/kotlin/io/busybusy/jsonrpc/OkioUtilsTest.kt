/**
 * Copyright 2018 busybusy, Inc.
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

package io.busybusy.jsonrpc

import okio.Buffer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OkioUtilsTest {
    val loremIpsumSample = """
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque facilisis rhoncus arcu, et vestibulum turpis lacinia eu. Fusce non finibus mauris. Duis a augue leo. Etiam ornare, augue et mollis cursus, lorem velit placerat ante, tempor volutpat quam risus dapibus velit. Nunc ultricies est et efficitur rhoncus. Nunc enim sapien, iaculis nec accumsan eget, semper quis est. Donec congue tortor nec eleifend fermentum. Curabitur at tincidunt risus, fermentum feugiat ipsum. Curabitur posuere, nisl non maximus fermentum, neque nisl pulvinar urna, a consequat eros purus id dui. Vestibulum dignissim, erat ac mattis vehicula, enim risus egestas lectus, sit amet egestas turpis velit vitae lorem. Fusce sollicitudin elementum malesuada. Nullam sed pretium elit. Integer a feugiat nisl, dignissim dictum ligula. Vestibulum dictum, sapien vel condimentum viverra, lorem lectus aliquet velit, et fermentum ipsum odio id sem. Aliquam non dignissim leo. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
        Duis et quam leo. Proin et ligula turpis. Proin at consequat tellus. Morbi cursus porta erat in vestibulum. Curabitur tortor ligula, rutrum sit amet vehicula nec, efficitur non nulla. Mauris in imperdiet est, ut rutrum metus. Donec feugiat iaculis pharetra. Morbi aliquam cursus facilisis. Donec aliquet vitae tellus quis dapibus. Nunc dapibus libero non dui malesuada, nec sagittis lorem tempus. Aenean arcu orci, posuere non dui eu, interdum molestie libero. Aenean tempor ultrices enim, vitae vehicula mi cursus nec. Fusce non aliquam augue, fringilla faucibus sapien. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam commodo orci eget nisi luctus egestas.
        Ut dictum nec enim in commodo. Aliquam erat volutpat. Fusce ut tristique arcu, vitae tincidunt erat. Sed non sollicitudin lacus, eget tempor elit. Aliquam tincidunt urna ut risus rhoncus porttitor. Pellentesque nec dignissim arcu. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Pellentesque facilisis, augue et molestie eleifend, nisi dolor consequat nisl, sit amet egestas nibh nisl id est. Aenean et magna et magna pellentesque pretium.
        Donec euismod varius orci, at consequat ligula sagittis vitae. Morbi ut metus blandit, tempus metus vitae, ullamcorper mi. Suspendisse quis tellus nec nunc posuere gravida. Integer aliquet dignissim lacus, sed eleifend enim finibus a. Morbi eget blandit augue. Morbi non sem ac justo consequat imperdiet. Suspendisse tincidunt ipsum ut dui pulvinar mattis. In luctus metus quis risus posuere consectetur. Nullam dictum leo ipsum, ut dignissim ipsum euismod a. Maecenas vitae ipsum at diam gravida finibus porttitor at purus. Curabitur elit enim, aliquam eget est tincidunt, ultrices fringilla diam. Vivamus sit amet sollicitudin orci. Fusce dictum ligula eu fermentum ultrices. Praesent posuere lorem id sapien malesuada pellentesque. Vivamus convallis diam at augue convallis fringilla.
        Mauris ac metus eu ex consequat dictum et at sem. Etiam eget mauris ut turpis dictum luctus id tempor sem. Cras quis erat lacus. Mauris posuere facilisis felis quis efficitur. Nunc ut mi eu elit finibus dapibus accumsan varius nisl. Duis eget scelerisque massa, ac scelerisque velit. Ut tincidunt ipsum sed velit fermentum lacinia. Curabitur quis enim quis enim varius eleifend. Vestibulum ornare sapien elementum, consequat nulla nec, laoreet ligula. Nullam condimentum odio sed felis euismod, vel iaculis neque malesuada. Aenean sodales placerat erat, et laoreet neque hendrerit eu. Integer accumsan erat dictum, imperdiet felis a, aliquam dolor. Sed fermentum euismod libero quis dapibus. Ut quis nunc vitae mauris malesuada egestas a faucibus lectus. Nullam vel massa id nisi tincidunt fringilla. Etiam accumsan sem ac est facilisis, vitae sagittis enim finibus.
    """.trimIndent()

    @Test
    fun `BufferedSource-copyTo(vararg BufferedSink) does a complete copy to the provided sinks`() {
        val source = Buffer().apply {
            writeUtf8(loremIpsumSample)
        }

        val (first, second, third) = source.copyTo(Buffer(), Buffer(), Buffer())

        assertEquals(loremIpsumSample, first.readUtf8())
        assertEquals(loremIpsumSample, second.readUtf8())
        assertEquals(loremIpsumSample, third.readUtf8())
    }
}