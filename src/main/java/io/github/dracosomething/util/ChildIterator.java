package io.github.dracosomething.util;

import java.util.ArrayList;
import java.util.List;

public class ChildIterator {
    private List<HTMLObject> children;
    private HTMLObject current;
    private HTMLObject previous;
    private HTMLObject next;
    private int currentIndex;

    public ChildIterator(HTMLObject current) {
        this.children = new ArrayList<>();
        this.initialize(current);
        this.currentIndex = 0;
        this.current = children.get(currentIndex);
        this.previous = null;
        if (this.currentIndex+1 == this.children.size()) {
            this.next = null;
        } else {
            this.next = this.children.get(this.currentIndex + 1);
        }
    }

    private void setNext() {
        this.previous = this.current;
        this.currentIndex++;
        this.current = this.next;
        if (this.currentIndex+1 == this.children.size()) {
            this.next = null;
        } else {
            this.next = this.children.get(this.currentIndex + 1);
        }
    }

    private void initialize(HTMLObject object) {
        children.add(object);
        if (object.hasLinkChildren()) {
            for (HTMLObject child : object.getChildObjects()) {
                initialize(child);
            }
        }
    }

    public boolean hasNext() {
        return this.next != null;
    }

    public HTMLObject next() {
        setNext();
        return this.current;
    }

    public HTMLObject peek() {
        return this.next;
    }

    public void skip() {
        setNext();
    }

    public HTMLObject previous() {
        if (this.previous != null) {
            this.next = this.current;
            this.current = this.previous;
            this.currentIndex--;
            if (this.currentIndex - 1 < 0) {
                this.previous = null;
            } else {
                this.previous = this.children.get(this.currentIndex-1);
            }
        } else {
            System.out.println("There is no object before the current one.");
        }
        return this.current;
    }
}
