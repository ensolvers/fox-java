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

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

/**
 * Service which simplifies metric pushing to AWS Cloudwatch
 *
 * @author José Matías Rivero
 */
public class CloudwatchService {

  private final String namespace;
  private final CloudWatchClient client;

  public CloudwatchService(
      String accessKeyId, String secretAccessKeyId, Region region, String namespace) {
    this.namespace = namespace;

    this.client = this.createClient(accessKeyId, secretAccessKeyId, region);
  }

  private CloudWatchClient createClient(
      String accessKeyId, String secretAccessKeyId, Region region) {
    CloudWatchClient client =
        CloudWatchClient.builder()
            .region(region)
            .credentialsProvider(
                new AwsCredentialsProvider() {
                  @Override
                  public AwsCredentials resolveCredentials() {
                    return AwsBasicCredentials.create(accessKeyId, secretAccessKeyId);
                  }
                })
            .build();

    return client;
  }

  /**
   * Publishes a new value for a specific metric
   *
   * @param dimensionName name for the dimension - e.g. <code>UNIQUE_PAGES</code>
   * @param dimensionValue a value for the dimension - e.g. <code>URLS</code>
   * @param value individual metric to be published for the dimension
   */
  public void put(String dimensionName, String dimensionValue, double value) {
    Dimension dimension = Dimension.builder().name(dimensionName).value(dimensionValue).build();

    MetricDatum datum =
        MetricDatum.builder()
            .metricName(dimensionValue)
            .unit(StandardUnit.NONE)
            .value(value)
            .dimensions(dimension)
            .build();

    PutMetricDataRequest request =
        PutMetricDataRequest.builder().namespace(this.namespace).metricData(datum).build();

    this.client.putMetricData(request);
  }
}
