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

import com.facebook.airlift.bootstrap.Bootstrap;
import com.facebook.airlift.log.Logger;
import com.facebook.drift.client.guice.DriftClientBinder;
import com.facebook.drift.transport.netty.client.DriftNettyClientModule;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Module;

import java.util.List;

import static com.facebook.drift.client.address.SimpleAddressSelectorBinder.simpleAddressSelector;

public class TestingNodeSelectorServiceClient
{
    private static final Logger log = Logger.get(TestingNodeSelectorServiceClient.class);

    public static void main(String[] args)
            throws Exception
    {
        List<HostAndPort> addresses = ImmutableList.of(HostAndPort.fromParts("localhost", 7779));

        ImmutableList.Builder<Module> modules = ImmutableList.builder();
        modules.add(
                new DriftNettyClientModule(),
                binder -> DriftClientBinder.driftClientBinder(binder).bindDriftClient(NodeSelectorService.class)
                        .withAddressSelector(simpleAddressSelector(addresses)));
        Bootstrap app = new Bootstrap(modules.build());
        try {
            NodeSelectorService instance = app.initialize().getInstance(NodeSelectorService.class);
            ListenableFuture<SelectNodesResult> allNodes = instance.allNodes();
            SelectNodesResult selectNodesResult = allNodes.get();
            System.out.println();
        }
        catch (Throwable e) {
            log.error(e);
            System.exit(1);
        }
    }
}
