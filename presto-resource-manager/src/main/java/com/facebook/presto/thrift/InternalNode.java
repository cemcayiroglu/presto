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

package com.facebook.presto.thrift;

import com.facebook.drift.annotations.ThriftConstructor;
import com.facebook.drift.annotations.ThriftField;
import com.facebook.drift.annotations.ThriftStruct;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Objects.requireNonNull;

@ThriftStruct
public class InternalNode
{
    private final String nodeIdentifier;
    private final String internalUri;
    private final String nodeVersion;
    private final boolean coordinator;

    @ThriftConstructor
    public InternalNode(String nodeIdentifier, String internalUri, String nodeVersion, boolean coordinator)
    {
        nodeIdentifier = emptyToNull(nullToEmpty(nodeIdentifier).trim());
        this.nodeIdentifier = requireNonNull(nodeIdentifier, "nodeIdentifier is null or empty");
        this.internalUri = requireNonNull(internalUri, "internalUri is null");
        this.nodeVersion = requireNonNull(nodeVersion, "nodeVersion is null");
        this.coordinator = coordinator;
    }

    @ThriftField(1)
    public String getNodeIdentifier()
    {
        return nodeIdentifier;
    }

    @ThriftField(2)
    public String getInternalUri()
    {
        return internalUri;
    }

    @ThriftField(3)
    public String getNodeVersion()
    {
        return nodeVersion;
    }

    @ThriftField(4)
    public boolean isCoordinator()
    {
        return coordinator;
    }
}
