/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package wisekb.flume.sink.jdbc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.instrumentation.SinkCounter;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractJDBCSink extends AbstractSink implements Configurable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJDBCSink.class);
    private CounterGroup counterGroup;
    private int batchSize;
    private SinkCounter counter;
    private JDBCConnectionManager connectionManager;

    protected abstract void prepareJDBC() throws SQLException;

    protected abstract void processJDBC(final Event event) throws Exception;

    protected abstract void completeJDBC() throws SQLException;

    protected abstract void abortJDBC() throws SQLException;

    @Override
    public void configure(final Context context) {
        if (counter == null) {
            counter = new SinkCounter(getName());
        }
        if (counterGroup == null) {
            counterGroup = new CounterGroup();
        }

        connectionManager = new JDBCConnectionManager(counter);
        connectionManager.configure(context);

        batchSize = context.getInteger("batchSize", 1);
        Preconditions.checkArgument(batchSize > 0, "Batch size must be specified and greater than zero.");
    }

    @Override
    public synchronized void start() {
        super.start();
        counter.start();
        connectionManager.start();
    }

    @Override
    public synchronized void stop() {
        super.stop();
        connectionManager.closeConnection();
        counter.stop();
    }

    public Connection getConnection() {
        return connectionManager.getConnection();
    }

    @Override
    public Status process() throws EventDeliveryException {
        final Channel channel = getChannel();
        final Transaction transaction = channel.getTransaction();

        try {
            transaction.begin();
            connectionManager.ensureConnectionValid();
            prepareJDBC();

            int count;
            for (count = 0; count < batchSize; count++) {
                final Event event = channel.take();

                if (event == null) {
                    break;
                }

                processJDBC(event);
            }

            completeJDBC();

            final Status status = updateAttemptCounters(count);
            connectionManager.getConnection().commit();
            transaction.commit();
            updateSuccessCounters(count);
            return status;
        } catch (Exception e) {

            try {
                abortJDBC();
                connectionManager.getConnection().rollback();
                transaction.rollback();
                updateFailureCounters();
            } catch (Exception e2) {
                LOG.error("Exception in rollback. Rollback might not have been successful.", e2);
            }

            LOG.error("Failed to commit transaction. Transaction rolled back.", e);
            Throwables.propagate(e);
        } finally {
            transaction.close();
        }

        return null;
    }

    @VisibleForTesting
    void setConnectionManager(final JDBCConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    private Status updateAttemptCounters(final int count) {
        counter.addToEventDrainAttemptCount(count);

        if (count == 0) {
            counter.incrementBatchEmptyCount();
            counterGroup.incrementAndGet("channel.underflow");
            return Status.BACKOFF;
        }

        if (count < batchSize) {
            counter.incrementBatchUnderflowCount();
            return Status.READY;
        }

        counter.incrementBatchCompleteCount();
        return Status.READY;
    }

    private void updateSuccessCounters(final int count) {
        counter.addToEventDrainSuccessCount(count);
        counterGroup.incrementAndGet("transaction.success");
    }

    private void updateFailureCounters() {
        counterGroup.incrementAndGet("transaction.rollback");
    }

}
