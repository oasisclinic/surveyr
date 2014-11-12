package models;

import org.jongo.Find;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MongoList<T> {

    private List<T> list = new LinkedList<>();

    public MongoList(Find find, Class<T> clazz) {

        Iterator<T> it = find.as(clazz).iterator();
        while(it.hasNext()) list.add(it.next());

    }

    public List<T> getList() {
        return list;
    }

}
