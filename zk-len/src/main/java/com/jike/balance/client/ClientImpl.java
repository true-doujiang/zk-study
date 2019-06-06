package com.jike.balance.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jike.balance.server.ServerData;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


public class ClientImpl implements Client {
		
	//提供基于负载均衡的算法
	private final BalanceProvider<ServerData> provider;
	
	//netty
	private EventLoopGroup group = null;
	private Channel channel = null;
	
	//日志
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	public ClientImpl(BalanceProvider<ServerData> provider) {
		this.provider = provider;
	}

	public void connect(){
		try{
			//获取负载最小的服务器
			ServerData serverData = provider.getBalanceItem();
			System.out.println("connecting to "+serverData.getHost()+":"+serverData.getPort()+", it's balance:"+serverData.getBalance());
			
			group = new NioEventLoopGroup();
			Bootstrap b = new Bootstrap();
			b.group(group)
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
		            @Override
		            public void initChannel(SocketChannel ch) throws Exception {
		                ChannelPipeline p = ch.pipeline();
		                p.addLast(new ClientHandler());
		            }
		        });
			
			ChannelFuture f = b.connect(serverData.getHost(),serverData.getPort()).syncUninterruptibly();
			channel = f.channel();
			
			System.out.println("started success!");
		}catch(Exception e){
			System.out.println("连接异常:"+e.getMessage());	
		}
	}

	public void disConnect(){
		try{
			if (channel!=null){
				channel.close().syncUninterruptibly();
			}
			
			group.shutdownGracefully();
			group = null;
			
			logger.debug("disconnected!");
			
		}catch(Exception e){
			logger.error(e.getMessage());
		}
	}

	public BalanceProvider<ServerData> getProvider() {
		return provider;
	}
}
