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

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The S3 service takes care of put,get and delete objects from S3
 *
 * @author Esteban Robles Luna
 */
public class S3Service {

  private static Logger logger = LoggerFactory.getLogger(S3Service.class);
  private static String LOG_PREFIX = "[AWS-S3-STORAGE]";

  private final AmazonS3Client s3Client;

  public S3Service(AmazonS3Client s3Client) {
    this.s3Client = s3Client;
  }

  /**
   * Sets the contents of file into the bucketName/keyName
   *
   * @param bucketName the bucket
   * @param keyName the path to the file
   * @param file the file to be uploaded
   */
  public void put(String bucketName, String keyName, File file) {
    try {
      logger.info(LOG_PREFIX + "[START] Uploading a new object to S3 from a file");

      PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, keyName, file);
      s3Client.putObject(putObjectRequest);

      logger.info(LOG_PREFIX + "[END] Uploading a new object to S3 from a file");
    } catch (AmazonServiceException ase) {
      logger.error(
          LOG_PREFIX
              + " Caught an AmazonServiceException, which "
              + "means your request made it "
              + "to Amazon S3, but was rejected with an error response"
              + " for some reason.",
          ase);
    } catch (AmazonClientException ace) {
      logger.error(
          LOG_PREFIX
              + " Caught an AmazonClientException, which "
              + "means the client encountered "
              + "an internal error while trying to "
              + "communicate with S3, "
              + "such as not being able to access the network.",
          ace);
    }
  }

  /**
   * Sets the contents of the MultipartFile into the bucketName/keyName This overload of the put
   * method simplifies the image uploading process to S3
   *
   * @param bucketName the bucket
   * @param keyName the path to the file
   */
  public String put(
      String bucketName, String keyName, InputStream inputStream, long size, boolean isPublicRead) {
    logger.info("{}[START] Uploading a new object to S3 from a file", LOG_PREFIX);

    var objectMetadata = new ObjectMetadata();
    objectMetadata.setContentLength(size);
    var request = new PutObjectRequest(bucketName, keyName, inputStream, objectMetadata);

    if (isPublicRead) {
      request.setCannedAcl(CannedAccessControlList.PublicRead);
    }

    s3Client.putObject(request);

    logger.info("{}[END] Uploading a new object to S3 from a file", LOG_PREFIX);

    return String.format("https://%s.s3.amazonaws.com/%s", bucketName, keyName);
  }

  /**
   * Gets the contents of the file in bucketName/keyName
   *
   * @param bucketName the bucketName
   * @param keyName the keyName
   * @return returns a local copy of the file in a temp directory
   */
  public File get(String bucketName, String keyName) {
    try {
      logger.info(LOG_PREFIX + "[START] Getting data of object of S3");

      GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, keyName);
      S3Object s3Object = s3Client.getObject(getObjectRequest);

      logger.info(LOG_PREFIX + "[END] Getting data of object of S3");

      File tmpFile = File.createTempFile("fox", "s3");
      S3ObjectInputStream inputStream = s3Object.getObjectContent();
      FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);

      IOUtils.copy(inputStream, fileOutputStream);
      IOUtils.closeQuietly(inputStream, null);
      IOUtils.closeQuietly(fileOutputStream, null);

      return tmpFile;
    } catch (AmazonServiceException ase) {
      logger.error(
          LOG_PREFIX
              + " Caught an AmazonServiceException, which "
              + "means your request made it "
              + "to Amazon S3, but was rejected with an error response"
              + " for some reason.",
          ase);
    } catch (AmazonClientException ace) {
      logger.error(
          LOG_PREFIX
              + " Caught an AmazonClientException, which "
              + "means the client encountered "
              + "an internal error while trying to "
              + "communicate with S3, "
              + "such as not being able to access the network.",
          ace);
    } catch (FileNotFoundException e) {
      logger.error(LOG_PREFIX + " not found", e);
    } catch (IOException e) {
      logger.error(LOG_PREFIX + " io error", e);
    }

    return null;
  }

  /**
   * Delete the object in bucketName/keyName
   *
   * @param bucketName the bucketName
   * @param keyName the keyName
   */
  public void delete(String bucketName, String keyName) {
    logger.info(LOG_PREFIX + "[START] Deleting a object of S3");

    try {
      DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, keyName);
      s3Client.deleteObject(deleteObjectRequest);

      logger.info(LOG_PREFIX + "[END] Deleting a object of S3");
    } catch (AmazonServiceException ase) {
      logger.error(
          LOG_PREFIX
              + " Caught an AmazonServiceException."
              + "Error Message:    "
              + ase.getMessage()
              + "HTTP Status Code: "
              + ase.getStatusCode()
              + "AWS Error Code:   "
              + ase.getErrorCode()
              + "Error Type:       "
              + ase.getErrorType()
              + "Request ID:       "
              + ase.getRequestId());
    } catch (AmazonClientException ace) {
      logger.error(
          LOG_PREFIX + " Caught an AmazonClientException." + "Error Message: " + ace.getMessage());
    }
  }

  /**
   * List all the files in buckey under the folderKey, returning the list of file names
   *
   * @param bucket the name of the buckey
   * @param folderKey the name of the container
   * @return the list of file names
   */
  public List<String> list(String bucket, String folderKey) {
    ListObjectsV2Request request =
        new ListObjectsV2Request().withBucketName(bucket).withPrefix(folderKey);

    ListObjectsV2Result result;
    List<String> keys = new ArrayList<>();
    do {
      result = this.s3Client.listObjectsV2(request);

      for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
        keys.add(objectSummary.getKey());
      }

      String token = result.getNextContinuationToken();
      request.setContinuationToken(token);

    } while (result.isTruncated());

    return keys;
  }
}
