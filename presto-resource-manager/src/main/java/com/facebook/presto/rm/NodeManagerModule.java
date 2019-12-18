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

import com.facebook.airlift.configuration.AbstractConfigurationAwareModule;
import com.facebook.airlift.configuration.ConfigBinder;
import com.facebook.airlift.discovery.server.EmbeddedDiscoveryModule;
import com.facebook.presto.client.NodeVersion;
import com.facebook.presto.client.ServerInfo;
import com.facebook.presto.execution.TaskManager;
import com.facebook.presto.failureDetector.FailureDetectorModule;
import com.facebook.presto.memory.LocalMemoryManager;
import com.facebook.presto.memory.NodeMemoryConfig;
import com.facebook.presto.metadata.DiscoveryNodeManager;
import com.facebook.presto.metadata.ForNodeManager;
import com.facebook.presto.metadata.InternalNodeManager;
import com.facebook.presto.server.GracefulShutdownModule;
import com.facebook.presto.server.InternalCommunicationModule;
import com.facebook.presto.server.ServerConfig;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import io.airlift.units.Duration;

import static com.facebook.airlift.configuration.ConfigBinder.configBinder;
import static com.facebook.airlift.discovery.client.DiscoveryBinder.discoveryBinder;
import static com.facebook.airlift.http.client.HttpClientBinder.httpClientBinder;
import static com.facebook.airlift.jaxrs.JaxrsBinder.jaxrsBinder;
import static com.facebook.airlift.json.JsonCodecBinder.jsonCodecBinder;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.reflect.Reflection.newProxy;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.weakref.jmx.guice.ExportBinder.newExporter;

public class NodeManagerModule
        extends AbstractConfigurationAwareModule
{
    @Override
    protected void setup(Binder binder)
    {
        ConfigBinder.configBinder(binder).bindConfig(ServerConfig.class);
        ServerConfig serverConfig = buildConfigObject(ServerConfig.class);
        // Determine the NodeVersion
        NodeVersion nodeVersion = new NodeVersion(serverConfig.getPrestoVersion());
        binder.bind(NodeVersion.class).toInstance(nodeVersion);

        discoveryBinder(binder).bindSelector("presto");
        binder.bind(DiscoveryNodeManager.class).in(Scopes.SINGLETON);
        binder.bind(InternalNodeManager.class).to(DiscoveryNodeManager.class).in(Scopes.SINGLETON);
        newExporter(binder).export(DiscoveryNodeManager.class).withGeneratedName();
        httpClientBinder(binder).bindHttpClient("node-manager", ForNodeManager.class)
                .withTracing()
                .withConfigDefaults(config -> {
                    config.setIdleTimeout(new Duration(30, SECONDS));
                    config.setRequestTimeout(new Duration(10, SECONDS));
                });

        install(new EmbeddedDiscoveryModule());
        install(new FailureDetectorModule());
        install(new InternalCommunicationModule());
        // presto coordinator announcement
        discoveryBinder(binder).bindHttpAnnouncement("presto-coordinator");
        // presto announcement
        discoveryBinder(binder).bindHttpAnnouncement("presto")
                .addProperty("node_version", nodeVersion.toString())
                .addProperty("coordinator", String.valueOf(serverConfig.isCoordinator()))
                .addProperty("connectorIds", nullToEmpty(serverConfig.getDataSources()));

        // server info resource
        jaxrsBinder(binder).bind(RMServerInfoResource.class);
        jsonCodecBinder(binder).bindJsonCodec(ServerInfo.class);

        configBinder(binder).bindConfig(NodeMemoryConfig.class);
        binder.bind(LocalMemoryManager.class).in(Scopes.SINGLETON);

        install(new GracefulShutdownModule());

        binder.bind(TaskManager.class).toInstance(newProxy(TaskManager.class, (proxy, method, args) -> {
            throw new UnsupportedOperationException();
        }));
    }
}
