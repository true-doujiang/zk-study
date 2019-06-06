
package com.jike.balance.server;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 处理与客户端的连接
 * 客户端连接上和断开连接时都需要修改当前服务器的负载信息   
 *  修改负载信息使用BalanceUpdateProvider
 *	
 */
public class ServerHandler extends ChannelHandlerAdapter{

	//修改服务器的负载信息
	private final BalanceUpdateProvider balanceUpdater;
	
	private static final Integer BALANCE_STEP = 1; 

    
    public ServerHandler(BalanceUpdateProvider balanceUpdater){
    	this.balanceUpdater = balanceUpdater;
    } 

    public BalanceUpdateProvider getBalanceUpdater() {
		return balanceUpdater;
	}	
	
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	System.out.println("one client connected...");
    	balanceUpdater.addBalance(BALANCE_STEP);
    }
	
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	balanceUpdater.reduceBalance(BALANCE_STEP);
    }

	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}
