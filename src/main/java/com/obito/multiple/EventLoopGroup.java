package com.obito.multiple;

import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author obito
 */
public class EventLoopGroup {

    EventLoop[] eventLoops;
    ServerSocketChannel server = null;
    AtomicInteger index = new AtomicInteger(0);

    EventLoopGroup eventLoopGroup = this;

    public EventLoopGroup(int num) throws Exception{
        this.eventLoops = new EventLoop[num];
        for (int i = 0; i < num; i++) {
            eventLoops[i] = new EventLoop(this);
            new Thread(eventLoops[i]).start();
        }
    }

    public void setWorker(EventLoopGroup worker) {
        this.eventLoopGroup = worker;
    }

    public void bind(int port) throws Exception{
        server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(new InetSocketAddress(port));

        bindEventLoop(server);
    }

    public void bindEventLoop(Channel channel) {
        if (channel instanceof ServerSocketChannel) {
            EventLoop eventLoop = nextBoss();
            eventLoop.setWorker(eventLoopGroup);
            eventLoop.queue.add(channel);
            eventLoop.selector.wakeup();
        }else {
            EventLoop eventLoop = nextWorker();
            eventLoop.queue.add(channel);
            eventLoop.selector.wakeup();
        }

    }

    public EventLoop nextBoss() {
        return eventLoops[index.incrementAndGet() % eventLoops.length];
    }

    public EventLoop nextWorker() {
        return eventLoopGroup.eventLoops[index.incrementAndGet() % eventLoopGroup.eventLoops.length];
    }


}
