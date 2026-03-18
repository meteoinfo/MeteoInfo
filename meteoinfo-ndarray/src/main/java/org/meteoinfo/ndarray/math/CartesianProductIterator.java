package org.meteoinfo.ndarray.math;

import org.meteoinfo.ndarray.Array;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An iterator that lazily generates the Cartesian product of a list of lists.
 *
 * <p>Given input like [[A, B], [1, 2], [X, Y]], it produces combinations:
 * [A,1,X], [A,1,Y], [A,2,X], ..., [B,2,Y] — one at a time, without storing
 * all results in memory.
 *
 * <p>This implementation uses an "index counter" approach (similar to a mixed-radix number),
 * where each dimension corresponds to one sublist, and indices are incremented with carry-over.
 *
 * <p>Memory usage is O(k), where k is the number of sublists (dimensions), making it suitable
 * for large-scale combinatorial problems where full enumeration would cause OOM.
 *
 */
public class CartesianProductIterator implements Iterator {

    /**
     * The original list of lists. Each inner list represents one dimension of choices.
     */
    private final List<Array> lists;

    /**
     * Current index in each sublist. For example, indices = [0, 1, 0] means:
     * - pick element 0 from lists.get(0)
     * - pick element 1 from lists.get(1)
     * - pick element 0 from lists.get(2)
     */
    private final int[] indices;

    /**
     * Flag indicating whether there is a next combination to generate.
     * Becomes false after the last combination is produced.
     */
    private boolean hasNext;

    /**
     * Constructs a Cartesian product iterator from the given list of lists.
     *
     * <p>Null sublists are filtered out. If any sublist is empty (or all are null/empty),
     * the iterator will have no elements (hasNext() returns false immediately).
     *
     * @param lists the list of lists to compute the Cartesian product of; may be null
     */
    public CartesianProductIterator(List<Array> lists) {
        if (lists == null) {
            this.lists = Collections.emptyList();
        } else {
            // Remove null sublists to avoid NullPointerException
            this.lists = lists.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        // Check if Cartesian product is possible: all sublists must be non-empty
        this.hasNext = !this.lists.isEmpty();
        for (Array list : this.lists) {
            if (list.getSize() == 0) {
                this.hasNext = false;
                break;
            }
        }

        // Initialize indices to zero (start from the first combination)
        this.indices = new int[this.lists.size()];
    }

    /**
     * Returns {@code true} if there is another combination available.
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return hasNext;
    }

    /**
     * Returns the next combination in the Cartesian product.
     *
     * <p>The returned list is a new {@code ArrayList} containing one element
     * from each sublist, based on the current {@link #indices}.
     *
     * <p>After returning the combination, the internal index counter is advanced
     * to prepare for the next call (like incrementing a mixed-radix number).
     *
     * @return the next combination as a {@code List<T>}
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public int[] next() {
        if (!hasNext) {
            throw new NoSuchElementException("No more combinations in Cartesian product.");
        }

        // Build the current combination using current indices
        int[] result = new int[lists.size()];
        for (int i = 0; i < lists.size(); i++) {
            result[i] = lists.get(i).getInt(indices[i]);
        }

        // Advance the index counter (simulate increment with carry)
        int position = indices.length - 1;
        while (position >= 0) {
            indices[position]++;
            // If current dimension hasn't overflowed, stop carrying
            if (indices[position] < lists.get(position).getSize()) {
                break;
            }
            // Reset this dimension to 0 and carry to the next higher dimension
            indices[position] = 0;
            position--;
        }

        // If we carried beyond the highest dimension, we're done
        if (position < 0) {
            hasNext = false;
        }

        return result;
    }

    /**
     * Removes from the underlying collection the last element returned by this iterator.
     *
     * <p>This operation is not supported for Cartesian product iterators,
     * as the combinations are computed on-the-fly and not stored.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove operation is not supported.");
    }
}
