package io.github.dracosomething.util;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ImprovedIterator<T> implements Iterator<T> {
    protected final T[] array;
    protected T previous;
    protected T current;
    protected T next;
    protected int currentIndex;

    public ImprovedIterator(T[] array) {
        this.array = array;
        this.currentIndex = 0;
        this.previous = null;
        this.current = array[currentIndex];
        if (this.currentIndex+1 == this.array.length) {
            this.next = null;
        } else {
            this.next = this.array[this.currentIndex+1];
        }
    }

    public ImprovedIterator(List<T> list) {
        this((T[]) list.toArray());
    }

    public static ImprovedIterator<Character> ofString(String str) {
        char[] arr = str.toCharArray();
        Character[] toUse = new Character[arr.length];
        for (int i = 0; i < arr.length; i++) {
            toUse[i] = arr[i];
        }
        return new ImprovedIterator<>(toUse);
    }

    protected void setNext() {
        this.previous = this.current;
        this.currentIndex++;
        this.current = this.next;
        if (this.currentIndex+1 == this.array.length) {
            this.next = null;
        } else {
            this.next = this.array[this.currentIndex+1];
        }
    }

    public boolean hasNext() {
        return this.next != null;
    }

    public T next() {
        setNext();
        return this.current;
    }

    public T peek() {
        return this.next;
    }

    public void skip() {
        setNext();
    }

    public T previous() {
        if (this.previous != null) {
            this.next = this.current;
            this.current = this.previous;
            this.currentIndex--;
            if (this.currentIndex - 1 < 0) {
                this.previous = null;
            } else {
                this.previous = this.array[this.currentIndex-1];
            }
        } else {
            System.out.println("There is no object before the current one.");
        }
        return this.current;
    }
}
