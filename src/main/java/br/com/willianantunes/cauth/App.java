package br.com.willianantunes.cauth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.cxf.helpers.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * If you have a target which requests a client certificate to connect with, you must first create a certificate, then 
 * ask the target to import the issuer (CA) which was used to sign your certificate and at last you can test the connection, but 
 * be aware that you must import the issuer of the target if it is not in your trust store.<br />
 * For the purpose of this lab, I do recommend to import the CA instead of disabling certificate validation.
 * @see <a href="http://stackoverflow.com/questions/2893819/telling-java-to-accept-self-signed-ssl-certificate">Telling java to accept self-signed ssl certificate</a>
 * @author Willian Antunes 
 */
public class App {
    public static void main( String[] args ) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
    	String myKeyStoreFile = "keystore.p12";
    	String myKeyStorePassword = "qwerty321";
    	String protocol = "TLS";
    	
		String keystorePath = Paths.get(ClassLoader.getSystemResource(myKeyStoreFile).toURI()).toFile().getAbsolutePath();

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] {
			    new X509TrustManager() {     
			        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; } 
			        public void checkClientTrusted(X509Certificate[] certs, String authType) { } 
			        public void checkServerTrusted(X509Certificate[] certs, String authType) { }
			    } 
			};  
	    
	    try {
	    	String myUrl = "https://your-test-server.com.br";
	    	URL url = new URL(myUrl);
	    	
		    // Disable hostname verification, otherwise you may get CertificateException for "No name matching my.test.com.br found"
		    HttpsURLConnection.setDefaultHostnameVerifier((String hostname, SSLSession session) -> true);		    
		    HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
		    
		    // If you want to ignore invalid certificates
		    conn.setSSLSocketFactory(getFactory(new File(keystorePath), myKeyStorePassword, trustAllCerts, protocol));
		    // Common usage
		    // conn.setSSLSocketFactory(getFactory(new File(keystorePath), myKeyStorePassword, null, protocol));
		    
		    InputStream inputstream = conn.getInputStream();
		    String result = IOUtils.toString(inputstream);
		   
		    // Pretty printing it
		    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		    Document document = documentBuilder.parse(new InputSource(new StringReader(result)));
			pretty(document, System.out, 2);
		} catch (IOException | SAXException | ParserConfigurationException | TransformerException e) {
			e.printStackTrace();
		}
	}

    /**
     * Create a SSL socket factory by the provided keystore and password.
     * @see <a href="http://vafer.org/blog/20061010073725/">Client cert authentication with java</a>
     */
	private static SSLSocketFactory getFactory(File pKeyFile, String pKeyPassword, TrustManager[] trustedCerts, String protocol) {
		SSLContext context = null;
		
		try {
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			KeyStore keyStore = KeyStore.getInstance("PKCS12");

			InputStream keyInput = new FileInputStream(pKeyFile);
			keyStore.load(keyInput, pKeyPassword.toCharArray());
			keyInput.close();

			keyManagerFactory.init(keyStore, pKeyPassword.toCharArray());

			context = SSLContext.getInstance(protocol); // You can pass TLS for example 
			context.init(keyManagerFactory.getKeyManagers(), trustedCerts, new SecureRandom());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return context.getSocketFactory();
	}

	private static void pretty(Document document, OutputStream outputStream, int indent) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        if (indent > 0) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
        }
        Result result = new StreamResult(outputStream);
        Source source = new DOMSource(document);
        transformer.transform(source, result);        
    }
}
