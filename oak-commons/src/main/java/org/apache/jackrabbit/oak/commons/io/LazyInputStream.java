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
package org.apache.jackrabbit.oak.commons.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import org.apache.commons.io.input.ClosedInputStream;

/**
 * * This input stream delays accessing the {@link InputStream} until the first byte is read
 */
public class LazyInputStream extends FilterInputStream {

    private final Supplier<InputStream> inputStreamSupplier;

    private boolean opened;

    public LazyInputStream(Supplier<InputStream> inputStreamSupplier) {
        super(null);
        this.inputStreamSupplier = inputStreamSupplier;
    }

    @Override
    public int read() throws IOException {
        ensureOpen();
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        ensureOpen();
        return super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        return super.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        ensureOpen();
        return super.skip(n);
    }

    @Override
    public int available() throws IOException {
        ensureOpen();
        return super.available();
    }

    @Override
    public void close() throws IOException {
        // make sure the file is not opened afterwards
        opened = true;

        // only close the file if it was in fact opened
        if (in != null) {
            super.close();
        } else {
            super.in = ClosedInputStream.CLOSED_INPUT_STREAM;
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        ensureOpen();
        super.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        ensureOpen();
        super.reset();
    }

    @Override
    public boolean markSupported() {
        ensureOpen();
        return super.markSupported();
    }

    private void ensureOpen() {
        if (!opened) {
            opened = true;
            super.in = inputStreamSupplier.get();
        }
    }

}
