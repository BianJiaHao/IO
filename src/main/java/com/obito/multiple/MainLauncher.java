package com.obito.multiple;

/**
 * 主启动器
 * @author obito
 */
public class MainLauncher {

    public static void main(String[] args) throws Exception{
        EventLoopGroup boss = new EventLoopGroup(3);
        EventLoopGroup worker = new EventLoopGroup(3);

        boss.bind(9090);
        boss.bind(9091);
        boss.bind(9092);

        boss.setWorker(worker);

    }

}
