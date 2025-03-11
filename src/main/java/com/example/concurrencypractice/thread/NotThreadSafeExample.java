package com.example.concurrencypractice.thread;

import lombok.Getter;

@Getter
class BankAccount {
    private int balance = 100;

    public void withdraw(int amount) {
        if (balance >= amount) { // 잔액 확인
            balance -= amount;   // 출금 처리
        }
    }

}

public class NotThreadSafeExample {
    public static void main(String[] args) {


        for (int i = 0; i < 10; i++) {
            System.out.println("잔액: " + concurrentWithDraw()); // 예상 결과는 20? 50? 0?
        }
        // 실제로 여러번 실행해보면
        /*
        잔액: 20
        잔액: 20
        잔액: 20
        잔액: 50 // 이렇게 다른 결과가 나오기도 함.
        잔액: 20
        잔액: 20
        잔액: 20
        잔액: 20
        잔액: 20
        잔액: 20
        * */
    }

    public static int concurrentWithDraw() {
        BankAccount account = new BankAccount();

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

/*
JVM은 기본적으로 스레드의 실행 순서를 보장하지 않기 때문에,
두 개의 스레드가 동시에 withdraw 메서드를 호출하면 서로의 상태를 고려하지 않고 연산이 진행되면서
예상과 다른 결과가 발생할 수 있게 되는 것이다.
 */