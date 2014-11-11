package de.hpi.semrecsys.webservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.hpi.semrecsys.spotlight.SpotlightConnector.EndpointType;

/**
 * base functionality for webservices
 */
public class WebServiceHelper {

	private final String CHARSET = "UTF-8";
	private final URLConnection connection;

	private String datatype;
	private final String basicUrl;
	private EndpointType endpointType;

	protected final Log log = LogFactory.getLog(getClass());

	public WebServiceHelper(String urlString, String datatype) {
		this.datatype = datatype;
		this.basicUrl = urlString;
		this.connection = getConnection(urlString, datatype);
	}

	private URLConnection getConnection(String urlString, String datatype) {
		URLConnection connection = null;
		try {
			connection = new URL(urlString).openConnection();
			connection.setDoOutput(true);
			// connection.setRequestProperty("Content-Type", datatype);
			connection.setRequestProperty("Accept", datatype);
			connection.setRequestProperty("Accept-Charset", CHARSET);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return connection;
	}

	public String readResponse() {
		String result = "";
		InputStream response;
		try {
			response = connection.getInputStream();
			result = readResponse(response);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	public String readResponse(PostMethod method) {
		InputStream rstream;
		String jsonString = null;
		HttpClient client = new HttpClient();

		try {
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				log.error("Requested uri: " + method.getURI());
				log.error("Method failed: " + method.getStatusLine() + " url: " + method.getPath() + " text: "
						+ method.getParameter("text"));
				return "{}";
			}
			rstream = method.getResponseBodyAsStream();
			// System.out.println("Response Charset: " +
			// method.getResponseCharSet());
			jsonString = readResponse(rstream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonString;
	}

	private String readResponse(InputStream rstream) {
		StringBuffer jsonString = new StringBuffer();
		BufferedReader br = null;
		br = new BufferedReader(new InputStreamReader(rstream));
		String line;
		try {
			while ((line = br.readLine()) != null) {
				jsonString.append(line);
				jsonString.append("\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeReader(br);
		}
		return jsonString.toString();
	}

	public void sendRequest(String query) {
		OutputStream output = null;
		try {
			output = connection.getOutputStream();
			output.write(query.getBytes(CHARSET));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeOutput(output);
		}
	}

	public String getDatatype() {
		return datatype;
	}

	public String getEndpointUrl() {
		return basicUrl + endpointType.getType();
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public void setEndpointType(EndpointType endpointType) {
		this.endpointType = endpointType;
	}

	public String getCHARSET() {
		return CHARSET;
	}

	private void closeReader(BufferedReader reader) {
		if (reader != null)
			try {
				reader.close();
			} catch (IOException ignore) {
			}
	}

	private void closeOutput(OutputStream output) {
		try {
			output.close();
		} catch (IOException logOrIgnore) {
		}
	}

}
