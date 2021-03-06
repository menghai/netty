/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec;

import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SocketUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.*;

public class DatagramPacketEncoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        channel = new EmbeddedChannel(
                new DatagramPacketEncoder<String>(
                        new StringEncoder(CharsetUtil.UTF_8)));
    }

    @After
    public void tearDown() {
        assertFalse(channel.finish());
    }

    @Test
    public void testEncode() {
        InetSocketAddress recipient = SocketUtils.socketAddress("127.0.0.1", 10000);
        InetSocketAddress sender = SocketUtils.socketAddress("127.0.0.1", 20000);
        assertTrue(channel.writeOutbound(
                new DefaultAddressedEnvelope<String, InetSocketAddress>("netty", recipient, sender)));
        DatagramPacket packet = channel.readOutbound();
        try {
            assertEquals("netty", packet.content().toString(CharsetUtil.UTF_8));
            assertEquals(recipient, packet.recipient());
            assertEquals(sender, packet.sender());
        } finally {
            packet.release();
        }
    }

    @Test
    public void testUnmatchedMessageType() {
        InetSocketAddress recipient = SocketUtils.socketAddress("127.0.0.1", 10000);
        InetSocketAddress sender = SocketUtils.socketAddress("127.0.0.1", 20000);
        DefaultAddressedEnvelope<Long, InetSocketAddress> envelope =
                new DefaultAddressedEnvelope<Long, InetSocketAddress>(1L, recipient, sender);
        assertTrue(channel.writeOutbound(envelope));
        DefaultAddressedEnvelope<Long, InetSocketAddress> output = channel.readOutbound();
        try {
            assertSame(envelope, output);
        } finally {
            output.release();
        }
    }

    @Test
    public void testUnmatchedType() {
        String netty = "netty";
        assertTrue(channel.writeOutbound(netty));
        assertSame(netty, channel.readOutbound());
    }
}
