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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

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

    AmazonS3Client client = new AmazonS3Client(new StaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)));
    S3Service service = new S3Service(client);

    //read non existant file
    File file = service.get(bucket ,"t1");
    Assert.assertNull(file);

    //no fail
    service.delete(bucket, "t1");

    //write file
    File f = File.createTempFile("ensolversfox", ".txt");
    FileUtils.writeStringToFile(f, testData, "UTF8");
    service.put(bucket, "t1", f);

    //read existant file
    file = service.get(bucket ,"t1");
    Assert.assertNotNull(file);
    String contents = FileUtils.readFileToString(file, "UTF8");
    Assert.assertEquals(testData, contents);

    //no fail
    service.delete(bucket, "t1");

    file = service.get(bucket ,"t1");
    Assert.assertNull(file);
  }
}
