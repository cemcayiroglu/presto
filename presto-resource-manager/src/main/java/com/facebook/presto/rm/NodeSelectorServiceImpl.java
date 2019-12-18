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

package com.facebook.presto.rm;

import com.facebook.airlift.log.Logger;
import com.facebook.presto.metadata.AllNodes;
import com.facebook.presto.metadata.InternalNodeManager;
import com.facebook.presto.thrift.InternalNode;
import com.facebook.presto.thrift.NodeSelectorException;
import com.facebook.presto.thrift.NodeSelectorService;
import com.facebook.presto.thrift.SelectNodesResult;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.facebook.airlift.concurrent.Threads.threadsNamed;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class NodeSelectorServiceImpl
        implements NodeSelectorService
{

    private static final Logger log = Logger.get(NodeSelectorServiceImpl.class);
    private final ListeningExecutorService executor;
    private final InternalNodeManager nodeManager;
    private final AtomicLong requestId = new AtomicLong();

    @Inject
    public NodeSelectorServiceImpl(NodeSelectorServiceConfig config, InternalNodeManager nodeManager)
    {
        this.executor = listeningDecorator(newFixedThreadPool(config.getNodeSelectorConcurrency(), threadsNamed("thrift-node-selector-%s")));
        this.nodeManager = nodeManager;
    }

    @Override
    public ListenableFuture<SelectNodesResult> allNodes()
    {
        return executor.submit(() -> {
            try {
                List<InternalNode> nodes = nodeManager.getAllNodes().getActiveNodes().stream().map(node ->
                        new InternalNode(node.getNodeIdentifier(), node.getHost(), node.getNodeVersion().getVersion(), node.isCoordinator()))
                        .collect(Collectors.toList());
                return new SelectNodesResult(nodes);
            }
            catch (Exception e) {
                log.error(e, e.getMessage());
                throw new NodeSelectorException(e.getMessage(), true);
            }
        });
    }

    @Override
    public ListenableFuture<SelectNodesResult> selectRandomNodes(int limit)
    {
        return executor.submit(() -> {
            try {
                return new SelectNodesResult(new ArrayList<>());
            }
            catch (Exception e) {
                log.error(e, e.getMessage());
                throw new NodeSelectorException(e.getMessage(), true);
            }
        });
    }
}
