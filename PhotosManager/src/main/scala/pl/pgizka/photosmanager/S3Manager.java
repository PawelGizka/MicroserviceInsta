package pl.pgizka.photosmanager;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;

import java.io.*;

public class S3Manager {
    private AmazonS3 s3;
    private final String bucketName = "jnpprojectphotos";

    public S3Manager() {
        s3 = new AmazonS3Client(new BasicAWSCredentials("AKIAJXHTICHVRJCFSQFA", "QT8MqzVV+MgSQYCLHRrHoTMTWM4oQWLBNvp7CNK6"));

    }

    public void upload(File file, String photoId) throws IOException {

        try {
            s3.putObject(new PutObjectRequest(bucketName, photoId, file).withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }


}