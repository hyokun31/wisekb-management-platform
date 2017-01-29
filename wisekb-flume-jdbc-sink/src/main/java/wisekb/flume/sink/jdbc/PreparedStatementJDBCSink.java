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

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PreparedStatementJDBCSink extends AbstractJDBCSink {

    private static final Logger LOG = LoggerFactory.getLogger(PreparedStatementJDBCSink.class);
    private String sql;
    private String messageParser;
    private Parser p;
    PreparedStatement preparedStatement;
    private PreparedStatementParser parser;

    @Override
    public void configure(final Context context) {
        super.configure(context);
        sql = context.getString("sql");
        messageParser = context.getString("parser");
        parser = new PreparedStatementParser(sql);

        try {
            Class clazz = Class.forName(messageParser);
            p = (Parser) clazz.newInstance();
        } catch (final Exception e) {
            LOG.error("Unable to create Message Parser '{}'.", messageParser, e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected void prepareJDBC() throws SQLException {
    }

    @Override
    protected void processJDBC(final Event event) throws Exception {
        Map parsed = p.parse(event);
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator(parser.getPreparedSQL(), new MapSqlParameterSource(parsed));
        preparedStatement = preparedStatementCreator.createPreparedStatement(getConnection());
        try {
            preparedStatement.executeUpdate();
        } finally {
            preparedStatement.close();
        }
    }

    protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource) {
        ParsedSql parsedSql = this.getParsedSql(sql);
        String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
        Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, (List) null);
        List declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, paramSource);
        PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sqlToUse, declaredParameters);
        return pscf.newPreparedStatementCreator(params);
    }

    protected ParsedSql getParsedSql(String sql) {
        return NamedParameterUtils.parseSqlStatement(sql);
    }

    @Override
    protected void completeJDBC() throws SQLException {
    }

    @Override
    protected void abortJDBC() {
        try {
            if (preparedStatement != null) preparedStatement.close();
        } catch (final SQLException e) {
            LOG.error("Unable to properly close statement on JDBC abort.", e);
        } finally {
            preparedStatement = null;
        }
    }

}
