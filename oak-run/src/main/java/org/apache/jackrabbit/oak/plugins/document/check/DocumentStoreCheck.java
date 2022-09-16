/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.plugins.document.check;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import com.google.common.io.Closer;

import org.apache.jackrabbit.oak.commons.concurrent.ExecutorCloser;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.apache.jackrabbit.oak.plugins.document.DocumentStore;
import org.apache.jackrabbit.oak.plugins.document.NodeDocument;
import org.apache.jackrabbit.oak.plugins.document.mongo.MongoDocumentStore;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.jackrabbit.oak.plugins.document.check.Result.END;
import static org.apache.jackrabbit.oak.plugins.document.mongo.MongoDocumentStoreCheckHelper.getAllNodeDocuments;
import static org.apache.jackrabbit.oak.plugins.document.mongo.MongoDocumentStoreCheckHelper.getEstimatedDocumentCount;
import static org.apache.jackrabbit.oak.plugins.document.util.Utils.getAllDocuments;

/**
 * <code>DocumentStoreCheck</code>...
 */
public class DocumentStoreCheck {

    private final DocumentNodeStore ns;

    private final DocumentStore store;

    private final Closer closer;

    private final boolean progress;

    private final boolean silent;

    private final boolean summary;

    private final int numThreads;

    private final String output;

    private final boolean orphan;

    private DocumentStoreCheck(DocumentNodeStore ns,
                               DocumentStore store,
                               Closer closer,
                               boolean progress,
                               boolean silent,
                               boolean summary,
                               int numThreads,
                               String output,
                               boolean orphan) {
        this.ns = ns;
        this.store = store;
        this.closer = closer;
        this.progress = progress;
        this.silent = silent;
        this.summary = summary;
        this.numThreads = numThreads;
        this.output = output;
        this.orphan = orphan;
    }

    public void run() throws Exception {
        BlockingQueue<Result> results = new LinkedBlockingQueue<>(1000);
        scheduleResultWriter(results);

        DocumentProcessor processor = createDocumentProcessor();
        for (NodeDocument doc : getAllDocs(store)) {
            processor.processDocument(doc, results);
        }
        processor.end(results);

        results.put(END);

    }

    private void scheduleResultWriter(BlockingQueue<Result> results)
            throws IOException {
        Consumer<String> log;
        if (silent) {
            log = s -> {};
        } else {
            log = System.out::println;
        }
        Consumer<String> out = log;
        if (output != null) {
            OutputStream os = Files.newOutputStream(Paths.get(output));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, UTF_8));
            closer.register(pw);
            out = log.andThen(pw::println);
        }
        ExecutorService writerService = Executors.newSingleThreadExecutor();
        closer.register(new ExecutorCloser(writerService));
        writerService.submit(new ResultWriter(results, out));
    }

    private DocumentProcessor createDocumentProcessor() {
        List<DocumentProcessor> processors = new ArrayList<>();
        if (summary) {
            processors.add(new Summary(numThreads));
        }
        if (progress) {
            DocumentProcessor p;
            if (store instanceof MongoDocumentStore) {
                MongoDocumentStore mds = (MongoDocumentStore) store;
                ETA eta = new ETA(getEstimatedDocumentCount(mds));
                p = new ProgressWithETA(eta);
            } else {
                p = new Progress();
            }
            processors.add(p);
        }
        if (orphan) {
            OrphanedNodeCheck onc = new OrphanedNodeCheck(ns, ns.getHeadRevision(), numThreads);
            closer.register(onc);
            processors.add(onc);
        }
        return CompositeDocumentProcessor.compose(processors);
    }

    private static Iterable<NodeDocument> getAllDocs(DocumentStore store) {
        if (store instanceof MongoDocumentStore) {
            return getAllNodeDocuments((MongoDocumentStore) store);
        } else {
            return getAllDocuments(store);
        }
    }

    public static class Builder {

        private final DocumentNodeStore ns;

        private final DocumentStore store;

        private final Closer closer;

        private boolean progress;

        private boolean silent;

        private boolean summary;

        private int numThreads = Runtime.getRuntime().availableProcessors();

        private String output;

        private boolean orphan;

        public Builder(DocumentNodeStore ns,
                       DocumentStore store,
                       Closer closer) {
            this.ns = ns;
            this.store = store;
            this.closer = closer;
        }

        public Builder withProgress(boolean enable) {
            this.progress = enable;
            return this;
        }

        public Builder isSilent(boolean enable) {
            this.silent = enable;
            return this;
        }

        public Builder withSummary(boolean enable) {
            this.summary = enable;
            return this;
        }

        public Builder withNumThreads(int numThreads) {
            this.numThreads = numThreads;
            return this;
        }

        public Builder withOutput(String outputFilePath) {
            this.output = outputFilePath;
            return this;
        }

        public Builder withOrphan(boolean enable) {
            this.orphan = enable;
            return this;
        }

        public DocumentStoreCheck build() {
            return new DocumentStoreCheck(ns, store, closer, progress, silent, summary, numThreads, output, orphan);
        }
    }

}
