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

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

public class CollectionUtilsTest {

    @Test
    public void ensureCapacity() {
        int capacity = CollectionUtils.ensureCapacity(8);
        Assert.assertEquals(11, capacity);
    }

    @Test
    public void ensureCapacityWithMaxValue() {
        int capacity = CollectionUtils.ensureCapacity(1073741825);
        Assert.assertEquals(1073741824, capacity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCapacityWithNegativeValue() {
        int capacity = CollectionUtils.ensureCapacity(-8);
        fail("Should throw IllegalArgumentException");
    }
}