package io.netty.example.echo;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import io.netty.util.ReferenceCountUtil;

/**
 * Handles a server-side channel.
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter { // (1)
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        try {
            // Echo the received data.
            ctx.write(msg);
        } finally {
            // ReferenceCountUtil.release(msg); // (3)
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
