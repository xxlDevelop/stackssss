package org.yx.hoststack.center.jobs.cmd;


import lombok.Getter;

@Getter
public class JobCmdChain {
    private Node<JobCmd<?>> head;

    public JobCmdChain next(JobCmd<?> data) {
        Node<JobCmd<?>> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
        } else {
            Node<JobCmd<?>> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        return this;
    }

    @Getter
    public static class Node<T> {
        T data;
        Node<T> next;

        // 构造函数
        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }
}
