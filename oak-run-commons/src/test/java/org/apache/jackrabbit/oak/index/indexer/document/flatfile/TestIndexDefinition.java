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
package org.apache.jackrabbit.oak.index.indexer.document.flatfile;

import org.apache.jackrabbit.oak.plugins.index.search.IndexDefinition;
import org.apache.jackrabbit.oak.plugins.memory.EmptyNodeState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class TestIndexDefinition extends IndexDefinition {

    private final String indexName;
    private final List<String> includedPaths;

    public TestIndexDefinition(String indexName, List<String> includedPaths) {
        super(EmptyNodeState.EMPTY_NODE, EmptyNodeState.EMPTY_NODE, "");
        this.indexName = indexName;
        this.includedPaths = includedPaths;
    }

    public TestIndexDefinition(List<@NotNull String> includedPaths) {
        this("test-index", includedPaths);
    }

    @Override
    public boolean shouldInclude(String path) {
        return includedPaths.stream().anyMatch(path::startsWith);
    }

    @Override
    public String getIndexName() {
        return indexName;
    }

}