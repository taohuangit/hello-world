package com.miao.client.foo;

import java.net.SocketAddress;

import com.miao.CmdManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class FooClient {
	
	private int port;
	
	public FooClient(int p) {
		this.port = p;
	}
	
	public void init() {
		EventLoopGroup group = new NioEventLoopGroup(1);

		try {
			ServerBootstrap server = new ServerBootstrap();
			server.group(group);
			server.channel(NioServerSocketChannel.class);
			server.option(ChannelOption.SO_BACKLOG, 1024);
			server.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel sc) throws Exception {
					sc.pipeline().addLast(new LengthFieldBasedFrameDecoder(4096, 0, 4, 20, 0));
					sc.pipeline().addLast(new InHandler());
				}
			});
			
			try {
				ChannelFuture future = server.bind(port).sync();
				
				future.channel().closeFuture().sync();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				group.shutdownGracefully();
			}
			
		} finally {
			
		}
	}
	
	static class OutHandler extends ChannelOutboundHandlerAdapter {
		@Override
		public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
			// TODO Auto-generated method stub
			super.disconnect(ctx, promise);
		}
		
		@Override
		public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
			// TODO Auto-generated method stub
			super.deregister(ctx, promise);
		}
		
		@Override
		public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
				throws Exception {
			// TODO Auto-generated method stub
			super.bind(ctx, localAddress, promise);
		}
		
		@Override
		public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
				ChannelPromise promise) throws Exception {
			// TODO Auto-generated method stub
			super.connect(ctx, remoteAddress, localAddress, promise);
		}
		@Override
		public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
			// TODO Auto-generated method stub
			super.close(ctx, promise);
		}
		
		@Override
		public void read(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.read(ctx);
		}
		
		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			// TODO Auto-generated method stub
			super.write(ctx, msg, promise);
		}
		
		@Override
		public void flush(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.flush(ctx);
		}
	}
	
	static class InHandler extends ChannelInboundHandlerAdapter {
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelActive(ctx);
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			// TODO Auto-generated method stub
			ByteBuf buf = (ByteBuf) msg;
			int n = buf.readableBytes();
			byte[] dst = new byte[n];
			buf.readBytes(dst);
//			System.out.println(BinHexUtil.binToHex(dst));
			CmdManager.cmd(ctx, dst);
		}
		
		@Override
		public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelRegistered(ctx);
		}
		
		@Override
		public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelUnregistered(ctx);
		}
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelInactive(ctx);
		}
		
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelReadComplete(ctx);
		}
		
		@Override
		public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelWritabilityChanged(ctx);
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			// TODO Auto-generated method stub
			super.exceptionCaught(ctx, cause);
		}
		
	}
	
	public static void main(String[] args) {
		new FooClient(8080).init();
	}
}
