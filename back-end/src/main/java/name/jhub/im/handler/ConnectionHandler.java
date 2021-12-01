package name.jhub.im.handler;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Iterator;

import name.jhub.im.session.LocalChannelManger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class ConnectionHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        //SocketAddress address = ctx.channel().remoteAddress();
        //LocalChannelManger.getInstance().addContext(address.toString(), ctx);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LocalChannelManger.getInstance().removeContext(ctx);
        syncRoster();
        SocketAddress address = ctx.channel().remoteAddress();
        System.out.println(address.toString() + "channelUnregistered");
        int count = LocalChannelManger.getInstance().staticClients();
        System.out.println("current clients : " + count);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        ByteBuf in = (ByteBuf) msg;
        String message = in.toString(Charset.forName("UTF-8"));
        // Flash沙箱处理
        String xml = "<?xml version=\"1.0\"?><cross-domain-policy><site-control permitted-cross-domain-policies=\"all\"/><allow-access-from domain=\"*\" to-ports=\"*\"/></cross-domain-policy>\0";
        if(message.trim().equals("<policy-file-request/>")){
            ctx.writeAndFlush(Unpooled.copiedBuffer(xml,CharsetUtil.UTF_8));
        }
        if(message.startsWith("AUTH:")){
            String name = (message.split(":"))[1];
            LocalChannelManger.getInstance().addContext(name, ctx);
            int count = LocalChannelManger.getInstance().staticClients();
            System.out.println("current clients : " + count);
            syncRoster();
        } else if (message.startsWith("MSG:")){
            String content = message.substring(4);
            String[] temp = content.split("#");
            String to = temp[0];
            String body = "";
            for(int i=1;i<temp.length;i++){
                if(i > 1){
                    body += "#";
                }
                body += temp[i];
            }
            if(LocalChannelManger.getInstance().isAvailable(to)){
                LocalChannelManger.getInstance().getContext(to).writeAndFlush(Unpooled.copiedBuffer(body,CharsetUtil.UTF_8));
            }
        } else if (message.startsWith("QUIT:")){
            String name = (message.split(":"))[1];
            LocalChannelManger.getInstance().removeContext(name);
            int count = LocalChannelManger.getInstance().staticClients();
            System.out.println("current clients : " + count);
            syncRoster();
        }
        System.out.println(message);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        //ctx.close();
        //System.out.println("server closed!");
    }

    // update all clients roster
    private void syncRoster(){
        String respone = "ROSTER:";
        for(String s : LocalChannelManger.getInstance().getAll()){
            respone += s + ",";
        }
        Iterator<ChannelHandlerContext> it = LocalChannelManger.getInstance().getAllClient().iterator();
        while(it.hasNext()){
            it.next().writeAndFlush(Unpooled.copiedBuffer(respone,CharsetUtil.UTF_8));
        }
    }

}
