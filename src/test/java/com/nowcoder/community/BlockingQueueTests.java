package com.nowcoder.community;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueTests {

    public static void testBlockQueue(){
        // 实例化一个BlockingQueue,最多存10个数
        BlockingQueue blockingQueue = new ArrayBlockingQueue(10);
        new Thread(new Producer(blockingQueue)).start();
        new Thread(new Consumer(blockingQueue)).start();
        new Thread(new Consumer(blockingQueue)).start();
        new Thread(new Consumer(blockingQueue)).start();


    }

    public static void main(String[] args) {
//        testBlockQueue();
    }
}

class Producer implements Runnable{

    // 要求调用方把阻塞队列传入进来
    private BlockingQueue<Integer> queue;

    public Producer(BlockingQueue<Integer> queue){
        this.queue = queue;
    }

    @Override
    public void run() {
        try{
            for (int i = 0; i < 100; i++) {
                // 设置一个间隔,每20ms生产一个数据
                Thread.sleep(20);
                queue.put(i);
                System.out.println(Thread.currentThread().getName() + "生产:" + queue.size());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

class Consumer implements Runnable{

    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue){
        this.queue = queue;
    }

    @Override
    public void run() {
        try{
            while(true){
                // 每次take就是从队列弹出队首的那个值
                // 服务器生产者生产消息的速度一般是固定的
                // 消费者消费的速度无论是产生一些数据或者是生成一些数据都是有一定的时间间隔的.这个时间就不写死了
                // 在0,1000之间随机一个数,大概虑随到的数比20更大
                Thread.sleep(new Random().nextInt(1000));

                // 就当成使用了这个数据
                queue.take();
                System.out.println(Thread.currentThread().getName() + "消费:" + queue.size());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}