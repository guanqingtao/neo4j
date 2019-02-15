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
package org.neo4j.kernel.recovery;

import java.io.IOException;

import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.transaction.log.ReadableClosablePositionAwareChannel;
import org.neo4j.kernel.impl.transaction.log.entry.LogEntryReader;
import org.neo4j.kernel.impl.transaction.log.entry.VersionAwareLogEntryReader;
import org.neo4j.kernel.impl.transaction.log.files.LogFiles;
import org.neo4j.kernel.impl.transaction.log.files.LogFilesBuilder;
import org.neo4j.kernel.monitoring.Monitors;
import org.neo4j.storageengine.api.StorageEngineFactory;

import static org.neo4j.kernel.recovery.RecoveryStartInformationProvider.NO_MONITOR;

/**
 * Utility that can determine if a given store will need recovery.
 */
public class RecoveryRequiredChecker
{
    private final FileSystemAbstraction fs;
    private final PageCache pageCache;
    private final Config config;
    private final StorageEngineFactory storageEngineFactory;

    RecoveryRequiredChecker( FileSystemAbstraction fs, PageCache pageCache, Config config, StorageEngineFactory storageEngineFactory )
    {
        this.fs = fs;
        this.pageCache = pageCache;
        this.config = config;
        this.storageEngineFactory = storageEngineFactory;
    }

    public boolean isRecoveryRequiredAt( DatabaseLayout databaseLayout ) throws IOException
    {
        LogTailScanner tailScanner = getLogTailScanner( databaseLayout );
        return isRecoveryRequiredAt( databaseLayout, tailScanner );
    }

    boolean isRecoveryRequiredAt( DatabaseLayout databaseLayout, LogTailScanner tailScanner )
    {
        if ( !storageEngineFactory.storageExists( fs, pageCache, databaseLayout ) )
        {
            // There was no store
            return false;
        }

        // We need config to determine where the logical log files are
        return new RecoveryStartInformationProvider( tailScanner, NO_MONITOR ).get().isRecoveryRequired();
    }

    private LogTailScanner getLogTailScanner( DatabaseLayout databaseLayout ) throws IOException
    {
        LogEntryReader<ReadableClosablePositionAwareChannel> reader = new VersionAwareLogEntryReader<>();
        LogFiles logFiles = LogFilesBuilder.activeFilesBuilder( databaseLayout, fs, pageCache )
                .withConfig( config )
                .withLogEntryReader( reader ).build();
        return new LogTailScanner( logFiles, reader, new Monitors() );
    }
}