/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.procedure.builtin;

import java.util.Map;
import java.util.Optional;

import org.neo4j.internal.kernel.api.CursorFactory;
import org.neo4j.internal.kernel.api.ExecutionStatistics;
import org.neo4j.internal.kernel.api.Locks;
import org.neo4j.internal.kernel.api.NodeCursor;
import org.neo4j.internal.kernel.api.Procedures;
import org.neo4j.internal.kernel.api.PropertyCursor;
import org.neo4j.internal.kernel.api.Read;
import org.neo4j.internal.kernel.api.RelationshipScanCursor;
import org.neo4j.internal.kernel.api.SchemaRead;
import org.neo4j.internal.kernel.api.SchemaWrite;
import org.neo4j.internal.kernel.api.Token;
import org.neo4j.internal.kernel.api.TokenRead;
import org.neo4j.internal.kernel.api.TokenWrite;
import org.neo4j.internal.kernel.api.Write;
import org.neo4j.internal.kernel.api.connectioninfo.ClientConnectionInfo;
import org.neo4j.internal.kernel.api.security.AuthSubject;
import org.neo4j.internal.kernel.api.security.SecurityContext;
import org.neo4j.internal.schema.IndexDescriptor;
import org.neo4j.internal.schema.IndexPrototype;
import org.neo4j.io.pagecache.tracing.cursor.PageCursorTracer;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.Statement;
import org.neo4j.kernel.api.exceptions.Status;
import org.neo4j.kernel.impl.api.ClockContext;
import org.neo4j.kernel.impl.coreapi.InternalTransaction;

public class StubKernelTransaction implements KernelTransaction
{

    StubKernelTransaction( )
    {
    }

    @Override
    public Statement acquireStatement()
    {
        return new StubStatement( );
    }

    @Override
    public IndexDescriptor indexUniqueCreate( IndexPrototype prototype )
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public long commit()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public void rollback()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public Read dataRead()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public Write dataWrite()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public TokenRead tokenRead()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public TokenWrite tokenWrite()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public Token token()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public SchemaRead schemaRead()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public SchemaWrite schemaWrite()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public Locks locks()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public void freezeLocks()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public void thawLocks()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public CursorFactory cursors()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public Procedures procedures()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public ExecutionStatistics executionStatistics()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public long closeTransaction()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public boolean isOpen()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public boolean isClosing()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public SecurityContext securityContext()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public ClientConnectionInfo clientInfo()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public AuthSubject subjectOrAnonymous()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public Optional<Status> getReasonIfTerminated()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public boolean isTerminated()
    {
        return false;
    }

    @Override
    public void markForTermination( Status reason )
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public long lastTransactionTimestampWhenStarted()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public long lastTransactionIdWhenStarted()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public void bindToUserTransaction( InternalTransaction internalTransaction )
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public InternalTransaction internalTransaction()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public long startTime()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public long startTimeNanos()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public long timeout()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public Type transactionType()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public long getTransactionId()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public long getCommitTime()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public Revertable overrideWith( SecurityContext context )
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public ClockContext clocks()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public NodeCursor ambientNodeCursor()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public RelationshipScanCursor ambientRelationshipCursor()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public PropertyCursor ambientPropertyCursor()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public void setMetaData( Map<String,Object> metaData )
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public Map<String,Object> getMetaData()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public void assertOpen()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public boolean isSchemaTransaction()
    {
        return false;
    }

    @Override
    public PageCursorTracer pageCursorTracer()
    {
        return null;
    }
}
