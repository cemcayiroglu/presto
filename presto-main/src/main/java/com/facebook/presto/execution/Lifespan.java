/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.presto.execution;

import com.facebook.drift.annotations.ThriftConstructor;
import com.facebook.drift.annotations.ThriftField;
import com.facebook.drift.annotations.ThriftStruct;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;

@ThriftStruct
public class Lifespan
{
    private static final Lifespan TASK_WIDE = new Lifespan("TaskWide");

    private final boolean grouped;
    private final int groupId;

    public static Lifespan taskWide()
    {
        return TASK_WIDE;
    }

    public static Lifespan driverGroup(int id)
    {
        return new Lifespan(true, id);
    }

    private Lifespan(boolean grouped, int groupId)
    {
        this.grouped = grouped;
        this.groupId = groupId;
    }

    public boolean isTaskWide()
    {
        return !grouped;
    }

    public int getId()
    {
        checkState(grouped);
        return groupId;
    }

    @ThriftConstructor
    public Lifespan(String value)
    {
        if (value.equals("TaskWide")) {
            this.grouped = false;
            this.groupId = 0;
        }
        else {
            checkArgument(value.startsWith("Group"));
            this.grouped = true;
            this.groupId = parseInt(value.substring("Group".length()));
        }
    }

    @JsonCreator
    public static Lifespan jsonCreator(String value)
    {
        if (value.equals("TaskWide")) {
            return Lifespan.taskWide();
        }
        checkArgument(value.startsWith("Group"));
        return Lifespan.driverGroup(parseInt(value.substring("Group".length())));
    }

    @ThriftField(value = 1, name = "value")
    @JsonValue
    public String toString()
    {
        return grouped ? "Group" + groupId : "TaskWide";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Lifespan that = (Lifespan) o;
        return grouped == that.grouped &&
                groupId == that.groupId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(grouped, groupId);
    }
}
