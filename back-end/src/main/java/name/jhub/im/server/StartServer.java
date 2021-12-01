package name.jhub.im.server;


import name.jhub.im.handler.ConnectionHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class StartServer {

    private int port;

    public StartServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        // Acceptor:threads default is availableProcessors * 2
        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        // Handler
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);
        try {
            ServerBootstrap server = new ServerBootstrap();
            ChannelHandler handler = new ChannelInitializer<SocketChannel>(){
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ConnectionHandler());
                }
            };
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(handler)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // Start the client
            ChannelFuture future = server.bind(port).sync();

            System.out.println("IM Server start");

            // Wait until the connection is closed
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new StartServer(8088).run();

    }
}
