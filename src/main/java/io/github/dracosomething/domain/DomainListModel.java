package io.github.dracosomething.domain;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class DomainListModel implements ListModel<CustomDomain>, Serializable {
    private final ArrayList<CustomDomain> data = new ArrayList<>();
    private final EventListenerList events = new EventListenerList();

    public DomainListModel() {
        data.addAll(CustomDomain.DOMAINS);
    }

    public DomainListModel(Collection<CustomDomain> collection) {
        data.addAll(collection);
    }

    public void add(CustomDomain domain) {
        this.data.add(domain);
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public CustomDomain getElementAt(int index) {
        return data.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        events.add(ListDataListener.class, l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        events.remove(ListDataListener.class, l);
    }
}
