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
package org.neo4j.unsafe.impl.batchimport.input;

import java.util.function.ToIntFunction;

import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.Values;

public class Inputs
{
    private Inputs()
    {
    }

    public static int calculatePropertySize( InputEntity entity, ToIntFunction<Value[]> valueSizeCalculator )
    {
        int size = 0;
        int propertyCount = entity.propertyCount();
        if ( propertyCount > 0 )
        {
            Value[] values = new Value[propertyCount];
            for ( int i = 0; i < propertyCount; i++ )
            {
                Object propertyValue = entity.propertyValue( i );
                values[i] = propertyValue instanceof Value ? (Value) propertyValue : Values.of( propertyValue );
            }
            size += valueSizeCalculator.applyAsInt( values );
        }
        return size;
    }
}