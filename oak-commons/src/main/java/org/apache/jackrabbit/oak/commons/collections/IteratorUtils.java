/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.jackrabbit.oak.commons.collections;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

/**
 * Utility methods for {@link Iterator} conversions.
 */
public class IteratorUtils {

    private IteratorUtils() {
        // no instances for you
    }
    /**
     * Convert an {@code Iterator} to an {@code Iterable}.
     * <p>
     * This method is not thread-safe
     *
     * @param iterator iterator to convert
     * @return a single-use iterable for the iterator (representing the remaining
     * elements in the iterator)
     * @throws IllegalStateException when {@linkplain Iterable#iterator()} is called more than
     *                               once
     */
    @NotNull
    public static <T> Iterable<T> toIterable(@NotNull final Iterator<T> iterator) {
        Objects.requireNonNull(iterator);

        return new Iterable<>() {

            private boolean consumed = false;

            @Override
            public @NotNull Iterator<T> iterator() {
                if (consumed) {
                    throw new IllegalStateException("Iterator already returned once");
                } else {
                    consumed = true;
                    return iterator;
                }
            }
        };
    }
}
