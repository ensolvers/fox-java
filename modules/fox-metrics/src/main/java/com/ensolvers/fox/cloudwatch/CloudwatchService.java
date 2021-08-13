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
   * Uses the default unit (StandardUnit.NONE)
   * Note that the dimensionValue is also used to set the name of the metric
   * @param dimensionName name for the dimension - e.g. <code>UNIQUE_PAGES</code>
   * @param dimensionValue a value for the dimension - e.g. <code>URLS</code>
   * @param value individual metric to be published for the dimension
   */
  public void put(String dimensionName, String dimensionValue, double value) {
    Dimension dimension = buildDimension(dimensionName, dimensionValue);
    MetricDatum datum = buildMetricDatum(dimensionValue, StandardUnit.NONE, dimension, value);
    PutMetricDataRequest request = buildRequest(datum);

    this.client.putMetricData(request);
  }

  /**
   * Publishes a new value for a specific metric
   * Uses the default unit (StandardUnit.NONE)
   * @param dimensionName name for the dimension - e.g. <code>UNIQUE_PAGES</code>
   * @param dimensionValue a value for the dimension - e.g. <code>URLS</code>
   * @param metricName the name of the metric - e.g <code>PAGES_VISITED</code>
   * @param value individual metric to be published for the dimension
   */
  public void put(String dimensionName, String dimensionValue, String metricName, double value) {
    Dimension dimension = buildDimension(dimensionName, dimensionValue);
    MetricDatum datum = buildMetricDatum(metricName, StandardUnit.NONE, dimension, value);
    PutMetricDataRequest request = PutMetricDataRequest.builder().namespace(this.namespace).metricData(datum).build();

    client.putMetricData(request);
  }

  /**
   * Publishes a new value for a specific metric
   * Uses the unit StandardUnit.MILLISECONDS
   * @param dimensionName name for the dimension - e.g. <code>UNIQUE_PAGES</code>
   * @param dimensionValue a value for the dimension - e.g. <code>URLS</code>
   * @param metricName the name of the metric - e.g <code>MILLISECONDS_TO_LOAD</code>
   * @param value individual metric to be published for the dimension
   */
  public void putMilliSeconds(String dimensionName, String dimensionValue, String metricName, double value) {
    Dimension dimension = buildDimension(dimensionName, dimensionValue);
    MetricDatum datum = buildMetricDatum(metricName, StandardUnit.MILLISECONDS, dimension, value);
    PutMetricDataRequest request = PutMetricDataRequest.builder().namespace(this.namespace).metricData(datum).build();

    client.putMetricData(request);
  }

  /**
   * Publishes a new value for a specific metric
   * Uses the unit StandardUnit.SECONDS
   * @param dimensionName name for the dimension - e.g. <code>UNIQUE_PAGES</code>
   * @param dimensionValue a value for the dimension - e.g. <code>URLS</code>
   * @param metricName the name of the metric - e.g <code>SECONDS_TO_LOAD</code>
   * @param value individual metric to be published for the dimension
   */
  public void putSeconds(String dimensionName, String dimensionValue, String metricName, double value) {
    Dimension dimension = buildDimension(dimensionName, dimensionValue);
    MetricDatum datum = buildMetricDatum(metricName, StandardUnit.SECONDS, dimension, value);
    PutMetricDataRequest request = PutMetricDataRequest.builder().namespace(this.namespace).metricData(datum).build();

    client.putMetricData(request);
  }

  /**
   * Publishes a new value for a specific metric
   * Uses the unit StandardUnit.COUNT
   * @param dimensionName name for the dimension - e.g. <code>UNIQUE_PAGES</code>
   * @param dimensionValue a value for the dimension - e.g. <code>URLS</code>
   * @param metricName the name of the metric - e.g <code>PAGES_VISITED</code>
   * @param value individual metric to be published for the dimension
   */
  public void putCount(String dimensionName, String dimensionValue, String metricName, double value) {
    Dimension dimension = buildDimension(dimensionName, dimensionValue);
    MetricDatum datum = buildMetricDatum(metricName, StandardUnit.COUNT, dimension, value);
    PutMetricDataRequest request = PutMetricDataRequest.builder().namespace(this.namespace).metricData(datum).build();

    client.putMetricData(request);
  }

  private Dimension buildDimension(String dimensionName, String dimensionValue) {
    return Dimension.builder().name(dimensionName).value(dimensionValue).build();
  }

  private MetricDatum buildMetricDatum(String metricName, StandardUnit standardUnit, Dimension dimension, double value) {
    return MetricDatum.builder().metricName(metricName).unit(standardUnit).value(value).dimensions(dimension).build();
  }

  private PutMetricDataRequest buildRequest(MetricDatum datum) {
    return PutMetricDataRequest.builder().namespace(this.namespace).metricData(datum).build();
  }
}
