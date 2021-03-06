package org.mazerunner.core.processor;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.mazerunner.core.algorithms;
import org.mazerunner.core.config.ConfigurationLoader;
import org.mazerunner.core.models.ProcessorMessage;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Copyright (C) 2014 Kenny Bastani
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
public class GraphProcessor {

    public static final String PROPERTY_GRAPH_UPDATE_PATH = "/neo4j/mazerunner/propertyUpdateList.txt";

    public static final String TRIANGLE_COUNT = "triangle_count";
    public static final String CONNECTED_COMPONENTS = "connected_components";
    public static final String PAGERANK = "pagerank";
    public static final String STRONGLY_CONNECTED_COMPONENTS = "strongly_connected_components";

    public static JavaSparkContext javaSparkContext = null;

    public static void processEdgeList(ProcessorMessage processorMessage) throws IOException, URISyntaxException {
        if(javaSparkContext == null) {
            initializeSparkContext();
        }

        String results = "";

        // Routing
        switch (processorMessage.getAnalysis()) {
            case PAGERANK:
                // Route to PageRank
                results = algorithms.pageRank(javaSparkContext.sc(), processorMessage.getPath());
                break;
            case CONNECTED_COMPONENTS:
                // Route to ConnectedComponents
                results = algorithms.connectedComponents(javaSparkContext.sc(), processorMessage.getPath());
                break;
            case TRIANGLE_COUNT:
                // Route to TriangleCount
                results = algorithms.triangleCount(javaSparkContext.sc(), processorMessage.getPath());
                break;
            case STRONGLY_CONNECTED_COMPONENTS:
                // Route to StronglyConnectedComponents
                results = algorithms.stronglyConnectedComponents(javaSparkContext.sc(), processorMessage.getPath());
                break;
            default:
                // Analysis does not exist
                System.out.println("Did not recognize analysis key: " + processorMessage.getAnalysis());
        }

        // Set the output path
        processorMessage.setPath(ConfigurationLoader.getInstance().getHadoopHdfsUri() + PROPERTY_GRAPH_UPDATE_PATH);

        // Write results to HDFS
        org.mazerunner.core.hdfs.FileUtil.writePropertyGraphUpdate(processorMessage, results);
    }

    public static void initializeSparkContext() {
        SparkConf conf = new SparkConf().setAppName(ConfigurationLoader.getInstance().getAppName()).set("spark.master", ConfigurationLoader.getInstance().getSparkHost())
                .set("spark.locality.wait", "3000")
                .set("spark.executor.memory", ConfigurationLoader.getInstance().getExecutorMemory());

        javaSparkContext = new JavaSparkContext(conf);
    }
}
