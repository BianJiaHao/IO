package com.obito.multiple;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author obito
 */
public class EventLoop implements Runnable {

    Selector selector = null;
    LinkedBlockingQueue<Channel> queue = new LinkedBlockingQueue<>();
    EventLoopGroup eventLoopGroup = null;

    public EventLoop(EventLoopGroup eventLoopGroup) throws Exception{
        this.eventLoopGroup = eventLoopGroup;
        this.selector = Selector.open();
    }

    @Override
    public void run() {

        while (true) {
            try {
                int fdSize = selector.select();
                if (fdSize > 0) {
                    Set<SelectionKey> fds = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = fds.iterator();
                    while(iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if(key.isAcceptable()){
                            acceptHandler(key);
                        }else if(key.isReadable()){
                            readHandler(key);
                        }else if(key.isWritable()){

                        }
                    }


                }
                if (!queue.isEmpty()) {
                    Channel c = queue.take();
                    if (c instanceof ServerSocketChannel) {
                        ServerSocketChannel server = (ServerSocketChannel) c;
                        server.register(this.selector,SelectionKey.OP_ACCEPT);
                        System.out.println(Thread.currentThread().getName() + "服务端注册");
                    }else {
                        SocketChannel client = (SocketChannel) c;
                        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
                        client.register(this.selector,SelectionKey.OP_READ,byteBuffer);
                        System.out.println(Thread.currentThread().getName() + "客户端注册" + client.getRemoteAddress());
                    }
                }



            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void readHandler(SelectionKey fd) throws Exception{
        System.out.println(Thread.currentThread().getName()+" read......");
        ByteBuffer buffer = (ByteBuffer) fd.attachment();
        SocketChannel client = (SocketChannel) fd.channel();
        buffer.clear();

        while (true) {
            try {
                int num = client.read(buffer);
                if (num > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        client.write(buffer);
                    }
                    buffer.clear();
                }else if (num == 0){
                    break;
                }else {
                    System.out.println(client.getRemoteAddress() + "断开了连接");
                    fd.cancel();
                    break;
                }
            }catch (Exception e) {
                System.out.println(client.getRemoteAddress() + "断开了连接");
                fd.cancel();
                break;
            }

        }
    }

    private void acceptHandler(SelectionKey fd) throws Exception{
        System.out.println(Thread.currentThread().getName() + " 接受了客户端");
        ServerSocketChannel server = (ServerSocketChannel) fd.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false);
        eventLoopGroup.bindEventLoop(client);
    }


    public void setWorker(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
    }
}
