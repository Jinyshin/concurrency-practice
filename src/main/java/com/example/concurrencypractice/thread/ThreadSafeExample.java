package com.example.concurrencypractice.thread;

class SafeBankAccount {
    private int balance = 100;

    public synchronized void withdraw(int amount) {
        if (balance >= amount) {
            balance -= amount;
        }
    }

    public synchronized int getBalance() {
        return balance;
    }
}

public class ThreadSafeExample {
    public static void main(String[] args) {


        for (int i = 0; i < 10; i++) {
            System.out.println("잔액: " + concurrentWithDraw()); // 예상 결과는 잔액: 20
        }
    }

    public static int concurrentWithDraw() {
        SafeBankAccount account = new SafeBankAccount();

        // 2개의 스레드가 동시에 출금 실행
        Thread t1 = new Thread(() -> account.withdraw(80)); // 80 출금
        Thread t2 = new Thread(() -> account.withdraw(50)); // 50 출금

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return account.getBalance();
    }
}
