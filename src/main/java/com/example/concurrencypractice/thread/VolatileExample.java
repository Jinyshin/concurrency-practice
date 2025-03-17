package com.example.concurrencypractice.thread;

class SharedData {
    volatile boolean flag = false; // 최신 값을 보장하기 위해 volatile 사용
}

class Worker extends Thread {
    SharedData data;

    Worker(SharedData data) {
        this.data = data;
    }

    public void run() {
        System.out.println("Worker 스레드 대기 중...");
        while (!data.flag) { // Main 스레드가 flag를 true로 바꿀 때까지 대기
            // busy-wait (바쁜 대기, CPU 자원을 소비할 수 있음)
        }
        System.out.println("Worker 스레드 종료!");
    }
}

public class VolatileExample {
    public static void main(String[] args) throws InterruptedException {
        SharedData sharedData = new SharedData();
        Worker worker = new Worker(sharedData);
        worker.start();

        Thread.sleep(3000); // 3초 후 flag 값을 변경
        sharedData.flag = true; // Main 스레드에서 flag 값을 true로 변경
        System.out.println("Main 스레드에서 flag 변경 완료!");
    }
}
