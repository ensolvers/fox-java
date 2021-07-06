/* Copyright (c) 2021 Ensolvers
 * All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2 alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to the project.
 *
 * You may obtain a copy of the LGPL License at: http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at: http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */
package com.ensolvers.fox.cloudwatch;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

/**
 * Service which simplifies metric pushing to AWS Cloudwatch
 *
 * @author José Matías Rivero
 */
public class CloudwatchService {

    private final String namespace;
    private final AmazonCloudWatch client;

    public CloudwatchService(
            String accessKeyId, String secretAccessKeyId, Regions region, String namespace) {
        this.namespace = namespace;

        this.client = this.createClient(accessKeyId, secretAccessKeyId, region);
    }

    private AmazonCloudWatch createClient(
            String accessKeyId, String secretAccessKeyId, Regions region) {
        AmazonCloudWatchClientBuilder cloudwatchClientBuilder =
                AmazonCloudWatchClientBuilder.standard();
        cloudwatchClientBuilder.setRegion(region.getName());
        cloudwatchClientBuilder.setCredentials(
                new AWSStaticCredentialsProvider(
                        new AWSCredentials() {
                            @Override
                            public String getAWSAccessKeyId() {
                                return accessKeyId;
                            }

                            @Override
                            public String getAWSSecretKey() {
                                return secretAccessKeyId;
                            }
                        }));
        return cloudwatchClientBuilder.build();
    }

    /**
     * Publishes a new value for a specific metric
     * @param dimensionName name for the dimension - e.g. <code>UNIQUE_PAGES</code>
     * @param dimensionValue a value for the dimension - e.g. <code>URLS</code>
     * @param value individual metric to be published for the dimension
     */
    public void put(String dimensionName, String dimensionValue, double value) {
        Dimension dimension = new Dimension()
                .withName(dimensionName)
                .withValue(dimensionValue);

        MetricDatum datum =
                new MetricDatum()
                        .withMetricName(dimensionValue)
                        .withUnit(StandardUnit.None)
                        .withValue(value)
                        .withDimensions(dimension);

        PutMetricDataRequest request =
                new PutMetricDataRequest()
                        .withNamespace(this.namespace)
                        .withMetricData(datum);

        this.client.putMetricData(request);
    }

}
