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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.*;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

/**
 * A Test case for {@link S3Service}
 *
 * @author Esteban Robles Luna
 */
public class S3ServiceTest {

  @Test
  public void testS3() throws Exception {
    String bucket = "hyros-foxtest";
    String testData = "this is a sample test data";
    String accessKey = "";
    String secretKey = "";

    AWSStaticCredentialsProvider credentials =
        new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
    AmazonS3Client client =
        (AmazonS3Client)
            AmazonS3ClientBuilder.standard()
                .withCredentials(credentials)
                .withRegion(Regions.DEFAULT_REGION)
                .build();
    S3Service service = new S3Service(client);

    // read non existent file
    File file = service.get(bucket, "t1");
    assertNull(file);

    // no fail
    service.delete(bucket, "t1");

    // write file
    File f = File.createTempFile("ensolversfox", ".txt");
    FileUtils.writeStringToFile(f, testData, "UTF8");
    service.put(bucket, "t1", f);

    // read existant file
    file = service.get(bucket, "t1");
    assertNotNull(file);
    String contents = FileUtils.readFileToString(file, "UTF8");
    assertEquals(testData, contents);

    // no fail
    service.delete(bucket, "t1");

    file = service.get(bucket, "t1");
    assertNull(file);

    List<String> keys = service.list(bucket, "folder");
    assertFalse(keys.isEmpty());
  }
}
