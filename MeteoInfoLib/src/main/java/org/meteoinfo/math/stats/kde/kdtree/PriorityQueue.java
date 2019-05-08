/**
 * %SVN.HEADER%
 * 
 * based on work by Simon Levy
 * http://www.cs.wlu.edu/~levy/software/kd/
 */
package org.meteoinfo.math.stats.kde.kdtree;

class PriorityQueue implements java.io.Serializable {

    /**
     * This class implements a <code>PriorityQueue</code>. This class is
     * implemented in such a way that objects are added using an
     * <code>add</code> function. The <code>add</code> function takes two
     * parameters an object and a long.
     * <p>
     * The object represents an item in the queue, the long indicates its
     * priority in the queue. The remove function in this class returns the
     * object first in the queue and that object is removed from the queue
     * permanently.
     * 
     * @author Bjoern Heckel
     * @version %I%, %G%
     * @since JDK1.2
     */

    /**
     * The maximum priority possible in this priority queue.
     */
    private double maxPriority = Double.MAX_VALUE;

    /**
     * This contains the list of objects in the queue.
     */
    private Object[] data;

    /**
     * This contains the list of prioritys in the queue.
     */
    private double[] value;

    /**
     * Holds the number of elements currently in the queue.
     */
    private int count;

    /**
     * This holds the number elements this queue can have.
     */
    private int capacity;

    /**
     * Creates a new <code>PriorityQueue</code> object. The
     * <code>PriorityQueue</code> object allows objects to be entered into the
     * queue and to leave in the order of priority i.e the highest priority
     * get's to leave first.
     */
    public PriorityQueue() {
        init(20);
    }

    /**
     * Creates a new <code>PriorityQueue</code> object. The
     * <code>PriorityQueue</code> object allows objects to be entered into the
     * queue an to leave in the order of priority i.e the highest priority get's
     * to leave first.
     * 
     * @param capacity
     *            the initial capacity of the queue before a resize
     */
    public PriorityQueue(int capacity) {
        init(capacity);
    }

    /**
     * Creates a new <code>PriorityQueue</code> object. The
     * <code>PriorityQueue</code> object allows objects to be entered into the
     * queue an to leave in the order of priority i.e the highest priority get's
     * to leave first.
     * 
     * @param capacity
     *            the initial capacity of the queue before a resize
     * @param maxPriority
     *            is the maximum possible priority for an object
     */
    public PriorityQueue(int capacity, double maxPriority) {
        this.maxPriority = maxPriority;
        init(capacity);
    }

    /**
     * This is an initializer for the object. It basically initializes an array
     * of long called value to represent the prioritys of the objects, it also
     * creates an array of objects to be used in parallel with the array of
     * longs, to represent the objects entered, these can be used to sequence
     * the data.
     * 
     * @param size
     *            the initial capacity of the queue, it can be resized
     */
    private void init(int size) {
        capacity = size;
        data = new Object[capacity + 1];
        value = new double[capacity + 1];
        value[0] = maxPriority;
        data[0] = null;
    }

    /**
     * This function adds the given object into the <code>PriorityQueue</code>,
     * its priority is the long priority. The way in which priority can be
     * associated with the elements of the queue is by keeping the priority and
     * the elements array entrys parallel.
     * 
     * @param element
     *            is the object that is to be entered into this
     *            <code>PriorityQueue</code>
     * @param priority
     *            this is the priority that the object holds in the
     *            <code>PriorityQueue</code>
     */
    public void add(Object element, double priority) {
        if (count++ >= capacity) {
            expandCapacity();
        }
        /* put this as the last element */
        value[count] = priority;
        data[count] = element;
        bubbleUp(count);
    }

    /**
     * Remove is a function to remove the element in the queue with the maximum
     * priority. Once the element is removed then it can never be recovered from
     * the queue with further calls. The lowest priority object will leave last.
     * 
     * @return the object with the highest priority or if it's empty null
     */
    public Object remove() {
        if (count == 0)
            return null;
        Object element = data[1];
        /* swap the last element into the first */
        data[1] = data[count];
        value[1] = value[count];
        /* let the GC clean up */
        data[count] = null;
        value[count] = 0L;
        count--;
        bubbleDown(1);
        return element;
    }

    public Object front() {
        return data[1];
    }

    public double getMaxPriority() {
        return value[1];
    }

    /**
     * Bubble down is used to put the element at subscript 'pos' into it's
     * rightful place in the heap (i.e heap is another name for
     * <code>PriorityQueue</code>). If the priority of an element at
     * subscript 'pos' is less than it's children then it must be put under one
     * of these children, i.e the ones with the maximum priority must come
     * first.
     * 
     * @param pos
     *            is the position within the arrays of the element and priority
     */
    private void bubbleDown(int pos) {
        Object element = data[pos];
        double priority = value[pos];
        int child;
        /* hole is position '1' */
        for (; pos * 2 <= count; pos = child) {
            child = pos * 2;
            /*
             * if 'child' equals 'count' then there is only one leaf for this
             * parent
             */
            if (child != count)

                /* left_child > right_child */
                if (value[child] < value[child + 1])
                    child++; /* choose the biggest child */
            /*
             * percolate down the data at 'pos', one level i.e biggest child
             * becomes the parent
             */
            if (priority < value[child]) {
                value[pos] = value[child];
                data[pos] = data[child];
            } else {
                break;
            }
        }
        value[pos] = priority;
        data[pos] = element;
    }

    /**
     * Bubble up is used to place an element relatively low in the queue to it's
     * rightful place higher in the queue, but only if it's priority allows it
     * to do so, similar to bubbleDown only in the other direction this swaps
     * out its parents.
     * 
     * @param pos
     *            the position in the arrays of the object to be bubbled up
     */
    private void bubbleUp(int pos) {
        Object element = data[pos];
        double priority = value[pos];
        /* when the parent is not less than the child, end */
        while (value[pos / 2] < priority) {
            /* overwrite the child with the parent */
            value[pos] = value[pos / 2];
            data[pos] = data[pos / 2];
            pos /= 2;
        }
        value[pos] = priority;
        data[pos] = element;
    }

    /**
     * This ensures that there is enough space to keep adding elements to the
     * priority queue. It is however advised to make the capacity of the queue
     * large enough so that this will not be used as it is an expensive method.
     * This will copy across from 0 as 'off' equals 0 is contains some important
     * data.
     */
    private void expandCapacity() {
        capacity = count * 2;
        Object[] elements = new Object[capacity + 1];
        double[] prioritys = new double[capacity + 1];
        System.arraycopy(data, 0, elements, 0, data.length);
        System.arraycopy(value, 0, prioritys, 0, data.length);
        data = elements;
        value = prioritys;
    }

    /**
     * This method will empty the queue. This also helps garbage collection by
     * releasing any reference it has to the elements in the queue. This starts
     * from offset 1 as off equals 0 for the elements array.
     */
    public void clear() {
        for (int i = 1; i < count; i++) {
            data[i] = null; /* help gc */
        }
        count = 0;
    }

    /**
     * The number of elements in the queue. The length indicates the number of
     * elements that are currently in the queue.
     * 
     * @return the number of elements in the queue
     */
    public int length() {
        return count;
    }

    // arbitrary; every serializable class has to have one of these
    public static final long serialVersionUID = 4L;

}