package de.hpi.semrecsys.webservice;

import java.net.URI;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.multipart.impl.MultiPartWriter;

public class YovistoClient {

	private final String username = "wolowyk";
	private final String password = "iephoHu2Goo3";

	private final Client client;
	private final HTTPBasicAuthFilter auth;

	public YovistoClient() {
		auth = new HTTPBasicAuthFilter(username, password);
		ClientConfig cc = new DefaultClientConfig();
		cc.getClasses().add(MultiPartWriter.class);
		client = Client.create(cc);

	}

	public String executeService(String serviceURL, String input, String acceptType) throws InterruptedException {
		WebResource inputResource = client.resource(serviceURL);
		inputResource.addFilter(auth);
		ClientResponse inputResponse = inputResource.post(ClientResponse.class, input);
		return processRequest(inputResponse, acceptType);
	}

	private String processRequest(ClientResponse inputResponse, String acceptType) throws InterruptedException {
		if (inputResponse.getStatus() == Status.PAYMENT_REQUIRED.getStatusCode())
			throw new RuntimeException("you need more payment to use this service");
		if (inputResponse.getStatus() == Status.BAD_REQUEST.getStatusCode())
			throw new RuntimeException("the given input was rejected for the following reason: "
					+ inputResponse.getEntity(String.class));
		if (inputResponse.getStatus() == Status.UNAUTHORIZED.getStatusCode())
			throw new RuntimeException("unauthorized. please check user name and password");
		return receiveResponse(inputResponse.getLocation(), acceptType);
	}

	private String receiveResponse(URI resultURI, String acceptType) throws InterruptedException {
		WebResource resultResource = client.resource(resultURI);
		resultResource.addFilter(auth);
		ClientResponse resultResponse = null;
		do {
			Builder resultAccept = resultResource.accept(acceptType);
			resultResponse = resultAccept.get(ClientResponse.class);
			// Thread.sleep(1000);
		} while (resultResponse.getStatus() == Status.ACCEPTED.getStatusCode());
		if (resultResponse.getStatus() == Status.NOT_ACCEPTABLE.getStatusCode())
			throw new RuntimeException("invalid content type for this service");
		if (resultResponse.getStatus() == Status.MOVED_PERMANENTLY.getStatusCode())
			resultResponse = client.resource(resultResponse.getLocation()).accept(acceptType).get(ClientResponse.class);
		return resultResponse.getEntity(String.class);
	}

}
