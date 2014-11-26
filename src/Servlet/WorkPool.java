package Servlet;
/*
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.util.json.JSONObject;

/**
 * Welcome to your new AWS Java SDK based project!
 *
 * This class is meant as a starting point for your console-based application
 * that makes one or more calls to the AWS services supported by the Java SDK,
 * such as EC2, SimpleDB, and S3.
 *
 * In order to use the services in this sample, you need:
 *
 * - A valid Amazon Web Services account. You can register for AWS at:
 * https://aws-portal.amazon.com/gp/aws/developer/registration/index.html
 *
 * - Your account's Access Key ID and Secret Access Key:
 * http://aws.amazon.com/security-credentials
 *
 * - A subscription to Amazon EC2. You can sign up for EC2 at:
 * http://aws.amazon.com/ec2/
 *
 * - A subscription to Amazon SimpleDB. You can sign up for Simple DB at:
 * http://aws.amazon.com/simpledb/
 *
 * - A subscription to Amazon S3. You can sign up for S3 at:
 * http://aws.amazon.com/s3/
 */
public class WorkPool extends Thread{

	/*
	 * WANRNING: To avoid accidental leakage of your credentials, DO NOT keep
	 * the credentials file in your source directory.
	 */
	private final static String USER_AGENT = "Mozilla/5.0";
	private final static String topicArn = "arn:aws:sns:us-east-1:668249848517:Cloud";
	private final static String myQueueUrl = "https://sqs.us-east-1.amazonaws.com/668249848517/Cloud";
	static AmazonSQSClient sqs;
	static AmazonSNSClient sns;

	/**
	 * The only information needed to create a client are security credentials
	 * consisting of the AWS Access Key ID and Secret Access Key. All other
	 * configuration, such as the service endpoints, are performed
	 * automatically. Client parameters, such as proxies, can be specified in an
	 * optional ClientConfiguration object when constructing a client.
	 *
	 * @see com.amazonaws.auth.BasicAWSCredentials
	 * @see com.amazonaws.auth.PropertiesCredentials
	 * @see com.amazonaws.ClientConfiguration
	 */
	private static void init() throws Exception {

		/*
		 * The ProfileCredentialsProvider will return your [default] credential
		 * profile by reading from the credentials file located at
		 * (/home/zeweijiang/.aws/credentials).
		 */
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("jiangzewei")
					.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. "
							+ "Please make sure that your credentials file is at the correct "
							+ "location (/home/zeweijiang/.aws/credentials), and is in valid format.",
					e);
		}
		sqs = new AmazonSQSClient(credentials);
		sns = new AmazonSNSClient(credentials);
	}

	public void run(){

		System.out.println("===========================================");
		System.out.println("Welcome to the AWS Java SDK!");
		System.out.println("===========================================");

		try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Send a message
		/*
		 * System.out.println("Sending a message to MyQueue.\n");
		 * sqs.sendMessage(new SendMessageRequest() .withQueueUrl(myQueueUrl)
		 * .withMessageBody("This is my message text."));
		 */
		/*
		 * List<Message> messages =getMessage(myQueueUrl);
		 * deleteMessage(myQueueUrl,messages); getMessage(myQueueUrl);
		 */

		PublishRequest publishRequest;

		while (true) {
			// System.out.println("Receiving messages from MyQueue.\n");
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(
					myQueueUrl);
			List<Message> messages = sqs.receiveMessage(receiveMessageRequest)
					.getMessages();
			for (Message message : messages) {

				String result = message.getBody();
				JSONObject jo;
				try {
					jo = new JSONObject(result);
					String res = sendPost((String) jo.get("text"));
				//System.out.println((String) jo.get("text"));
				//System.out.println((String) jo.get("time"));
				publishRequest = new PublishRequest(topicArn,
						(String) jo.get("text"));
				if (res.equals(""))
					res = "error";
				publishRequest.addMessageAttributesEntry("senti",
						new MessageAttributeValue().withDataType("String")
								.withStringValue(res));

				/*
				 * publishRequest.addMessageAttributesEntry( "text", new
				 * MessageAttributeValue().withDataType("String")
				 * .withStringValue( (String)jo.get("text")));
				 */
				publishRequest.addMessageAttributesEntry("lon",
						new MessageAttributeValue().withDataType("String")
								.withStringValue((String) jo.get("lon")));
				publishRequest.addMessageAttributesEntry("lat",
						new MessageAttributeValue().withDataType("String")
								.withStringValue((String) jo.get("lat")));
				publishRequest.addMessageAttributesEntry("time",
						new MessageAttributeValue().withDataType("String")
								.withStringValue((String) jo.get("time")));
				publishRequest.addMessageAttributesEntry("id",
						new MessageAttributeValue().withDataType("String")
								.withStringValue((String) jo.get("id")));
				PublishResult publishResult = sns.publish(publishRequest);
				deleteMessage(myQueueUrl, message);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	private static void deleteMessage(String myQueueUrl, Message message) {
		// Delete a message
		//System.out.println("Deleting a message.\n");
		String messageReceiptHandle = message.getReceiptHandle();
		sqs.deleteMessage(new DeleteMessageRequest().withQueueUrl(myQueueUrl)
				.withReceiptHandle(messageReceiptHandle));

	}

	private static String sendPost(String text) throws Exception {
		String url = "http://access.alchemyapi.com/calls/text/TextGetTextSentiment";
		URL obj = new URL(url);
		URLConnection con = obj.openConnection();

		// add reuqest header

		((HttpURLConnection) con).setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		String urlParameters = "apikey=fa3b074c840b31e6663252959738f76dd6a11182&text="
				+ text;
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = ((HttpURLConnection) con).getResponseCode();
		//System.out.println("\nSending 'POST' request to URL : " + url);

		//System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		//System.out.println(response.toString());

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = (Document) db.parse(new InputSource(
				new StringReader(response.toString())));
		Node n = document.getFirstChild();
		NodeList nl = n.getChildNodes();
		Node an, an2;

		String fff = "";
		for (int i = 0; i < nl.getLength(); i++) {
			an = nl.item(i);
			if (an.getNodeType() == Node.ELEMENT_NODE) {
				NodeList nl2 = an.getChildNodes();

				for (int i2 = 0; i2 < nl2.getLength(); i2++) {
					an2 = nl2.item(i2);
					// DEBUG PRINTS
					if (an2.getNodeName().equalsIgnoreCase("score"))
						fff = an2.getTextContent();
				}

			}
		}
		return fff;

	}
}
