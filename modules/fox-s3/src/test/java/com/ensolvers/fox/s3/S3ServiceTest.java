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
package com.ensolvers.fox.s3;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.*;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * A Test case for {@link S3Service}
 *
 * @author Esteban Robles Luna
 */
@Testcontainers
public class S3ServiceTest {
  // Test constants
  private final String BUCKET_NAME = "foxtest";
  private final String KEY = "t1";
  private final String FILENAME = "ensolversfox";

  @Container
  public LocalStackContainer localstack =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.11.3"))
          .withServices(LocalStackContainer.Service.S3);

  @Test
  public void testS3() throws Exception {
    String testData = "this is a sample test data";
    String folderName = "f1";
    String fileSuffix = ".txt";
    String charset = "UTF8";

    AmazonS3Client client =
        (AmazonS3Client)
            AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                    localstack.getEndpointConfiguration(LocalStackContainer.Service.S3))
                .withCredentials(localstack.getDefaultCredentialsProvider())
                .build();

    S3Service service = new S3Service(client);
    // prepares the bucket
    client.createBucket(BUCKET_NAME);

    // read non existent file
    File file = service.get(BUCKET_NAME, KEY);
    assertNull(file);

    // no fail
    service.delete(BUCKET_NAME, KEY);

    // write file in1 root context
    File f = File.createTempFile(FILENAME, fileSuffix);
    FileUtils.writeStringToFile(f, testData, charset);
    service.put(BUCKET_NAME, KEY, f);
    f.delete();

    // read existent file
    file = service.get(BUCKET_NAME, KEY);
    assertNotNull(file);
    String contents = FileUtils.readFileToString(file, charset);
    assertEquals(testData, contents);

    // no fail
    service.delete(BUCKET_NAME, KEY);

    file = service.get(BUCKET_NAME, KEY);
    assertNull(file);

    List<String> keys = service.list(BUCKET_NAME, folderName);
    assertTrue(keys.isEmpty());

    // write file in folder
    f = File.createTempFile(FILENAME, fileSuffix);
    FileUtils.writeStringToFile(f, testData, charset);
    service.put(BUCKET_NAME, folderName + "/" + KEY, f);
    f.delete();

    // now folder shouldn't be empty
    keys = service.list(BUCKET_NAME, folderName);
    assertFalse(keys.isEmpty());
  }

  @Test
  public void shouldGeneratePresignedUrl() {
    Long secondsToExpire = 60L;

    AmazonS3Client client =
        (AmazonS3Client)
            AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                    localstack.getEndpointConfiguration(LocalStackContainer.Service.S3))
                .withCredentials(localstack.getDefaultCredentialsProvider())
                .build();

    S3Service service = new S3Service(client);

    String presignedUrl = service.generatePresignedUrl(BUCKET_NAME, KEY, secondsToExpire, FILENAME);

    assertNotNull(presignedUrl);
    assertFalse(presignedUrl.isEmpty());
    assertFalse(presignedUrl.isBlank());
  }
}
