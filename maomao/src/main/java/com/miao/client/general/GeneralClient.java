package com.miao.client.general;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miao.COMMAND;
import com.miao.CmdManager;
import com.miao.Connection;
import com.miao.Connection.RoomStatus;

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

public class GeneralClient {
	
	private int port;
	
	public GeneralClient(int p) {
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
					sc.pipeline().addLast(new LengthFieldBasedFrameDecoder(4096, 0, 4, 2, 0));
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
				bossgroup.shutdownGracefully();
				workergroup.shutdownGracefully();
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
		
		private Connection conn;
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			conn = new Connection();
			conn.setConnId(ctx.channel().id().toString());
			conn.setCtx(ctx);
			conn.setRooms(null);
			conn.setUser(null);
			conn.setRoomStatus(RoomStatus.INIT);
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			ByteBuf buf = (ByteBuf) msg;
			int n = buf.readableBytes();
			byte[] dst = new byte[n];
			buf.readBytes(dst);
			
			CharsetDecoder decoder = Charset.forName("utf-8").newDecoder();
			
			JSONObject json = JSON.parseObject(dst, 0, dst.length-0, decoder, JSONObject.class);
			
			int cmd = json.getIntValue("cmd");
			switch (cmd) {
			case COMMAND.INTO_ROOMS:
				
				break;
			case COMMAND.SEND_BARRAGE:
				break;

			case COMMAND.LOGIN_REQUEST:
				
				break;
			default:
				break;
			}
		}
		
		@Override
		public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelRegistered(ctx);
			log("channelRegistered", " ", Thread.currentThread());
		}
		
		@Override
		public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelUnregistered(ctx);
			log("channelUnregistered", " ", Thread.currentThread());
		}
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelInactive(ctx);
			log("channelInactive", " ", Thread.currentThread());
		}
		
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelReadComplete(ctx);
			log("channelReadComplete", " ", Thread.currentThread());
		}
		
		@Override
		public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelWritabilityChanged(ctx);
			log("channelWritabilityChanged", " ", Thread.currentThread());
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			// TODO Auto-generated method stub
			super.exceptionCaught(ctx, cause);
			log("exceptionCaught", " ", Thread.currentThread());
		}
		
	}

	private static void log(Object ... objects) {
		for (Object o : objects) {
//			System.out.print(o);
		}
//		System.out.println();
	}
	
	public static void main(String[] args) {
		new GeneralClient(8080).init();
	}
}
