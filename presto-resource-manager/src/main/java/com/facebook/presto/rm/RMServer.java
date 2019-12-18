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

import com.facebook.airlift.bootstrap.Bootstrap;
import com.facebook.airlift.discovery.client.Announcer;
import com.facebook.airlift.discovery.client.DiscoveryModule;
import com.facebook.airlift.event.client.HttpEventModule;
import com.facebook.airlift.http.server.HttpServerModule;
import com.facebook.airlift.jaxrs.JaxrsModule;
import com.facebook.airlift.jmx.JmxModule;
import com.facebook.airlift.jmx.http.rpc.JmxHttpRpcModule;
import com.facebook.airlift.json.JsonModule;
import com.facebook.airlift.log.Logger;
import com.facebook.airlift.node.NodeModule;
import com.facebook.airlift.tracetoken.TraceTokenModule;
import com.facebook.drift.transport.netty.server.DriftNettyServerModule;
import com.facebook.presto.server.GracefulShutdownModule;
import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.weakref.jmx.guice.MBeanModule;

public class RMServer
{
    private static final Logger log = Logger.get(RMServer.class);

    private RMServer() {}

    public static void main(String[] args)
            throws Exception
    {
        ImmutableList.Builder<Module> modules = ImmutableList.builder();
        modules.add(
                new DriftNettyServerModule(),
                new NodeSelectorServiceModule(),
                new MBeanModule(),
                new NodeModule(),
                new HttpServerModule(),
                new JaxrsModule(true),
                new JsonModule(),
                new JmxModule(),
                new JmxHttpRpcModule(),
                new HttpEventModule(),
                new TraceTokenModule(),
                new DiscoveryModule(),
                new TraceTokenModule(),
                new NodeManagerModule(),
                new GracefulShutdownModule(),
                binder -> {

                }
        );

        try {
            Bootstrap app = new Bootstrap(modules.build());
            Injector injector = app.strictConfig().initialize();
            injector.getInstance(Announcer.class).start();
            log.info("======== PRESTO RM SERVER STARTED ========");
        }
        catch (Throwable e) {
            log.error(e);
            System.exit(1);
        }
    }
}
