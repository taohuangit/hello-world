package com.miao.protocol;

import java.util.HashSet;
import java.util.Set;

import com.miao.util.BinHexUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class Server {
	
	private int port;
	
	public Server(int p) {
		this.port = p;
	}
	
	public void init() {
		EventLoopGroup bossgroup = new NioEventLoopGroup();
		EventLoopGroup workergroup = new NioEventLoopGroup();
		System.out.println(bossgroup);
		System.out.println(workergroup);

		try {
			ServerBootstrap server = new ServerBootstrap();
			server.group(bossgroup, workergroup);
			server.channel(NioServerSocketChannel.class);
			server.option(ChannelOption.SO_BACKLOG, 1024);
			server.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel sc) throws Exception {
					System.out.println(sc);
					sc.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 20, 0));
					sc.pipeline().addLast(new ProtocolHandler());
				}
			});
			
			
			try {
				ChannelFuture future = server.bind(port).sync();
				
				future.channel().closeFuture().sync();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				bossgroup.shutdownGracefully();
				workergroup.shutdownGracefully();
			}
			
			
			
		} finally {
			// TODO: handle finally clause
		}
	}
	
	static class ProtocolHandler extends ChannelHandlerAdapter {
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelActive(ctx);
			ctx.writeAndFlush(null);
			
			System.out.println("channelActive: " + System.currentTimeMillis() + ctx);
		}
		
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelReadComplete(ctx);
			System.out.println("channelReadComplete: " + System.currentTimeMillis());
		}
		
		@Override
		public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelUnregistered(ctx);
			System.out.println("channelUnregistered: " + System.currentTimeMillis());
		}
		
		@Override
		public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelWritabilityChanged(ctx);
			System.out.println("channelWritabilityChanged: " + System.currentTimeMillis());
		}
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelInactive(ctx);
			System.out.println("channelInactive: " + System.currentTimeMillis());
		}
		
		@Override
		public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelRegistered(ctx);
			System.out.println("channelRegistered: " + System.currentTimeMillis());
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			ByteBuf buf = (ByteBuf) msg;
			byte[] nbyte = new byte[buf.readableBytes()];
			buf.readBytes(nbyte);
						
			System.out.println(BinHexUtil.BinaryToHexString(nbyte));
		}
	}
		
	public static void main(String[] args) {
		new Server(8080).init();
	}
}
