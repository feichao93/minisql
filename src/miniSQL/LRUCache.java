package miniSQL;

import java.util.*;

import static java.text.MessageFormat.format;

public class LRUCache<K, V> {
    public static void main(String[] args) {
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);
        cache.set(1, 2);
        cache.set(2, 4);
        cache.set(3, 6); // LRUCache{ <3, 6>, <2, 4>, <1, 2> }
        System.out.println(cache.get(1)); // LRUCache{ <1, 2>, <3, 6>, <2, 4> }
        cache.set(4, 8); // <2,4> will be removed from the cache
        System.out.println(cache); // LRUCache{ <4, 8>, <1, 2>, <3, 6> }
    }

    private final int capacity;
    private Map<K, Node<K, V>> map;
    private Node<K, V> head;
    private Node<K, V> tail;

    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "LRUCache{ ", " }");
        Node node = head;
        while (node != tail) {
            joiner.add(format("<{0}, {1}>", node.key, node.value));
            node = node.next;
        }
        if (tail != null) {
            joiner.add(format("<{0}, {1}>", tail.key, tail.value));
        }
        return joiner.toString();
    }

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new RuntimeException("Illegal capacity: " + capacity);
        }
        this.capacity = capacity;
        map = new HashMap<>(capacity);
    }

    /**
     * 从缓存中获取key对应的value. O(1)的时间复杂度.
     *
     * @param key 要查询的键值
     * @return key对应的value. 如果缓存中不存在key对应的value, 则返回null
     */
    public V get(K key) {
        if (!map.containsKey(key)) {
            return null;
        }
        Node<K, V> node = map.get(key);
        removeNodeFromList(node); // 将node从双向链表中取下来
        putNodeAtHead(node); // 将node放在链表头, 表示最近访问
        return node.value;
    }

    /**
     * 向缓存中key/value对. 如果缓存已满, 则最长时间未被使用的key/value对将被移除. O(1)的时间复杂度.
     */
    public void set(K key, V value) {
        if (map.containsKey(key)) { // key已经存在于cache中
            Node<K, V> node = map.get(key);
            removeNodeFromList(node);
            // 更新node的位置和value
            putNodeAtHead(node);
            node.value = value;
        } else { // key不在cache中
            if (map.size() == capacity) { // cache已满, 移除tail位置的Node
                Node<K, V> popped = popTail();
                map.remove(popped.key);
            }
            // 新增node, 并将其放置于head处
            Node<K, V> node = new Node<>(key, value);
            putNodeAtHead(node);
            map.put(key, node);
        }
    }

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * 将{@code node}放置在内部的双薪链表的头部
     *
     * @param node 要放置的结点
     */
    private void putNodeAtHead(Node<K, V> node) {
        if (head == null) {
            head = node;
            tail = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }
    }

    /**
     * 移除内部双向链表的尾部结点.
     *
     * @return 返回被移除的结点
     */
    private Node<K, V> popTail() {
        Node<K, V> popped = tail;
        tail = popped.prev;
        if (tail != null) {
            tail.next = null;
        }
        return popped;
    }

    /**
     * 将{@code node}从内部的双向链表中移除
     *
     * @param node 要移除的结点
     */
    private void removeNodeFromList(Node<K, V> node) {
        if (node == head) {
            head = head.next;
        } else if (node == tail) {
            tail = tail.prev;
        } else {
            Node prev = node.prev;
            Node next = node.next;
            prev.next = next;
            next.prev = prev;
        }
    }
}
