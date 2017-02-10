package org.mule.modules.opc.config;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.mule.api.ConnectionException;
import org.mule.api.ConnectionExceptionCode;
import org.mule.api.annotations.Connect;
import org.mule.api.annotations.ConnectionIdentifier;
import org.mule.api.annotations.Disconnect;
import org.mule.api.annotations.TestConnectivity;
import org.mule.api.annotations.ValidateConnection;
import org.mule.api.annotations.components.ConnectionManagement;
import org.mule.api.annotations.param.ConnectionKey;
import org.mule.modules.opc.OPCConnector;
import org.mule.modules.opc.entity.CustomNodeId;
import org.mule.modules.opc.entity.NodeIdMetaData;
import org.mule.modules.opc.utility.UtilMethod;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.ApplicationType;
import org.opcfoundation.ua.core.Argument;
import org.opcfoundation.ua.core.IdType;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.core.ReferenceDescription;
import org.opcfoundation.ua.transport.security.HttpsSecurityPolicy;
import org.opcfoundation.ua.transport.security.SecurityMode;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.PkiFileBasedCertificateValidator;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.SessionActivationException;
import com.prosysopc.ua.UaAddress;
import com.prosysopc.ua.UserIdentity;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.client.ConnectException;
import com.prosysopc.ua.client.InvalidServerEndpointException;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.nodes.MethodArgumentException;
import com.prosysopc.ua.nodes.UaDataType;
import com.prosysopc.ua.nodes.UaMethod;


@ConnectionManagement(friendlyName = "Configuration")
public class ConnectorConfig {

	  private UaClient client;
	  private static String APP_NAME = "SampleConnector";
	  protected SecurityMode securityMode = SecurityMode.NONE;

	  private NodeId rootNode = Identifiers.RootFolder;
	  private List<NodeIdMetaData> nodes;
	  private List<NodeIdMetaData> methods;
	  
		protected String userName;
		protected int sessionCount = 0;
	   // private String userName;
		
		public UaClient getClient() {
			return client;
		}



		public List<NodeIdMetaData> getNodes() throws ServiceResultException {
			buildNodes();
			return nodes;
		}

		
		public List<NodeIdMetaData> getMethods() throws ServiceResultException{
			buildNodes();
			return methods;
		}


		public void setNodes(List<NodeIdMetaData> nodes) {
			this.nodes = nodes;
		}



		@Connect
	    @TestConnectivity
	    public void connect(@ConnectionKey String passWord, String serverUri) throws ConnectionException {
	    	
	    	
	    	try{
		    		
	    		UaAddress.validate(serverUri);
		    	client = new UaClient(serverUri);
		
				// Use PKI files to keep track of the trusted and rejected server
				// certificates...
				final PkiFileBasedCertificateValidator validator = new PkiFileBasedCertificateValidator();
				client.setCertificateValidator(validator);
				// ...and react to validation results with a custom handler (to prompt
				// the user what to do, if necessary)
				//validator.setValidationListener(validationListener);
		
				// *** Application Description is sent to the server
				ApplicationDescription appDescription = new ApplicationDescription();
				// 'localhost' (all lower case) in the ApplicationName and
				// ApplicationURI is converted to the actual host name of the computer
				// in which the application is run
				appDescription.setApplicationName(new LocalizedText(APP_NAME + "@localhost"));
				appDescription.setApplicationUri("urn:localhost:OPCUA:" + APP_NAME);
				appDescription.setProductUri("urn:prosysopc.com:OPCUA:" + APP_NAME);
				appDescription.setApplicationType(ApplicationType.Client);
		
			/*	// *** Certificates
		
				File privatePath = new File(validator.getBaseDir(), "private");
		
				// Create self-signed certificates
				KeyPair issuerCertificate = null;
		
				// Enable the following to define a CA certificate which is used to
				// issue the keys.
		
				// issuerCertificate =
				// ApplicationIdentity.loadOrCreateIssuerCertificate(
				// "ProsysSampleCA", privatePath, "opcua", 3650, false);
		
				int[] keySizes = null;
				// If you wish to use big certificates (4096 bits), you will need to
				// define two certificates for your application, since to interoperate
				// with old applications, you will also need to use a small certificate
				// (up to 2048 bits).
		
				// 4096 bits can only be used with Basic256Sha256 security profile,
				// which is currently not enabled by default, so we will also not define
				// this by default.
		
				// Use 0 to use the default keySize and default file names as before
				// (for other values the file names will include the key size).
				// keySizes = new int[] { 0, 4096 };
		
				// *** Application Identity
				// Define the client application identity, including the security
				// certificate
				final ApplicationIdentity identity = ApplicationIdentity.loadOrCreateCertificate(appDescription,
						"Sample Organisation",  Private Key Password "opcua",
						 Key File Path privatePath,
						 CA certificate & private key issuerCertificate,
						 Key Sizes for instance certificates to create keySizes,
						 Enable renewing the certificate true);
		
				// Create the HTTPS certificate.
				// The HTTPS certificate must be created, if you enable HTTPS.
				String hostName = InetAddress.getLocalHost().getHostName();
				identity.setHttpsCertificate(ApplicationIdentity.loadOrCreateHttpsCertificate(appDescription, hostName, "opcua",
						issuerCertificate, privatePath, true));
		
				client.setApplicationIdentity(identity);
		*/
				// Define our user locale - the default is Locale.getDefault()
				client.setLocale(Locale.ENGLISH);
		
				// Define the call timeout in milliseconds. Default is null - to
				// use the value of UaClient.getEndpointConfiguration() which is
				// 120000 (2 min) by default
				client.setTimeout(30000);
		
				// StatusCheckTimeout is used to detect communication
				// problems and start automatic reconnection.
				// These are the default values:
				client.setStatusCheckTimeout(10000);
				// client.setAutoReconnect(true);
		
				// Listen to server status changes
				//client.addServerStatusListener(serverStatusListener);
		
				// Define the security mode
				// - Default (in UaClient) is BASIC128RSA15_SIGN_ENCRYPT
				// client.setSecurityMode(SecurityMode.BASIC128RSA15_SIGN_ENCRYPT);
				// client.setSecurityMode(SecurityMode.BASIC128RSA15_SIGN);
				// client.setSecurityMode(SecurityMode.NONE);
		
				// securityMode is defined from the command line
				client.setSecurityMode(securityMode);
		
				// Define the security policies for HTTPS; ALL is the default
				client.getHttpsSettings().setHttpsSecurityPolicies(HttpsSecurityPolicy.ALL);
		
				// Define a custom certificate validator for the HTTPS certificates
				client.getHttpsSettings().setCertificateValidator(validator);
		
				// Or define just a validation rule to check the hostname defined for
				// the certificate; ALLOW_ALL_HOSTNAME_VERIFIER is the default
				// client.getHttpsSettings().setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		
				// If the server supports user authentication, you can set the user
				// identity.
				if (userName == null)
					// - Default is to use Anonymous authentication, like this:
					client.setUserIdentity(new UserIdentity());
				else {
					// - Use username/password authentication (note requires security,
					// above):
					/*if (passWord == null) {
						print("Enter password for user " + userName + ":");
						passWord = readInput(false);
					}*/
					client.setUserIdentity(new UserIdentity(userName, passWord));
				}
				// - Read the user certificate and private key from files:
				// client.setUserIdentity(new UserIdentity(new java.net.URL(
				// "my_certificate.der"), new java.net.URL("my_protectedkey.pfx"),
				// "my_protectedkey_password"));
		
				// Session timeout 10 minutes; default is one hour
				// client.setSessionTimeout(600000);
		
				// Set endpoint configuration parameters
				client.getEndpointConfiguration().setMaxByteStringLength(Integer.MAX_VALUE);
				client.getEndpointConfiguration().setMaxArrayLength(Integer.MAX_VALUE);
		
				// TCP Buffer size parameters - these may help with high traffic
				// situations.
				// See http://fasterdata.es.net/host-tuning/background/ for some hints
				// how to use them
				// TcpConnection.setReceiveBufferSize(700000);
				// TcpConnection.setSendBufferSize(700000);
				
				if(!client.isConnected()){
					client.setSessionName(String.format("%s@%s/Session%d", APP_NAME,
							ApplicationIdentity.getActualHostNameWithoutDomain(), ++sessionCount));

					client.connect();
				}
				
	    	}
	    	catch(SessionActivationException e){
	    		throw new ConnectionException(ConnectionExceptionCode.INCORRECT_CREDENTIALS, null, e.getMessage(), e);
	    		
	    	}catch(URISyntaxException e){
	    		throw new ConnectionException(ConnectionExceptionCode.UNKNOWN_HOST, null, e.getMessage(), e);
	    		
	    	}catch(InvalidServerEndpointException e){
	    		throw new ConnectionException(ConnectionExceptionCode.UNKNOWN_HOST, null, e.getMessage(), e);
	    		
	    	}catch(ConnectException e){
	    		throw new ConnectionException(ConnectionExceptionCode.CANNOT_REACH, null, e.getMessage(), e);
	    		
	    	}catch(ServiceException e){
	    		throw new ConnectionException(ConnectionExceptionCode.UNKNOWN, null, e.getMessage(), e);
	    	}

	    }
	    
	    private void buildNodes() throws ServiceResultException{
	    	nodes = new LinkedList<>();
	    	methods = new LinkedList<>();
	    	CustomNodeId customRootNode = UtilMethod.getCustomNodeId(rootNode);
	    	NodeIdMetaData rootMetaData = new NodeIdMetaData();
	    	rootMetaData.setNodeId(customRootNode);
	    	rootMetaData.setReferenceDescription(null);
	    	nodes.add(rootMetaData);
	    	searchChildNodes(rootMetaData, nodes,methods);
	    }
	    
	    
	    private void searchChildNodes(NodeIdMetaData node,List<NodeIdMetaData> nodes,List<NodeIdMetaData> methods) throws ServiceResultException{
			List<ReferenceDescription> references = getReferences(node.getNodeId());
			List<NodeIdMetaData> childs = new LinkedList<>();
			List<NodeIdMetaData> childMethods = new LinkedList<>();	
			for(ReferenceDescription r: references){
				CustomNodeId customnodeId = UtilMethod.getCustomNodeId(client.getAddressSpace().getNamespaceTable().toNodeId(r.getNodeId()));
				NodeIdMetaData rootMetaData = new NodeIdMetaData(r,customnodeId);
				searchChildNodes(rootMetaData,childs,methods);
				if(r.getNodeClass() == NodeClass.Method){
					childMethods.add(rootMetaData);
				}
				childs.add(rootMetaData);
			}
			nodes.addAll(childs);
			methods.addAll(childMethods);
		}
	    
	    private  List<ReferenceDescription> getReferences(CustomNodeId customNodeId)   {
	    	NodeId nodeId = CustomNodeId.get(customNodeId.getIdType(), customNodeId.getNamespaceIndex(), customNodeId.getValue());
			//printCurrentNode(nodeId);
			// client.getAddressSpace().setReferenceTypeId(ReferencesToReturn);
			List<ReferenceDescription> references;
			// Find the reference to use for browsing up: prefer the previous node,
			// but otherwise accept any hierarchical inverse reference
			//List<ReferenceDescription> upReferences = null;
			try {
				client.getAddressSpace().setMaxReferencesPerNode(1000);
				references = client.getAddressSpace().browse(nodeId);
				//for (int i = 0; i < references.size(); i++)
					//printf("%d - %s\n", i, referenceToString(references.get(i)));
				//upReferences = client.getAddressSpace().browseUp(nodeId);
			} catch (Exception e) {
				//printException(e);
				references = new ArrayList<ReferenceDescription>();
				//upReferences = new ArrayList<ReferenceDescription>();
			}
			
			return references;
			
	    }

	    /**
	     * Disconnect
	     */
	    @Disconnect
	    public void disconnect() {
	    	client.disconnect();;
	    }

	    /**
	     * Are we connected
	     * 
	     * @return true/false Connected or not
	     */
	    @ValidateConnection
	    public boolean isConnected() {
	    	return client!=null&&client.isConnected();
	    }

	    /**
	     * Are we connected
	     * 
	     * @return ConnectionId Connection Id
	     */

	    @ConnectionIdentifier
	    public String connectionId() {
	        return null;
	    }


	
	public static void main(String[] a) throws ServiceResultException, MethodArgumentException, ServiceException, AddressSpaceException{
			OPCConnector conn = new OPCConnector();
	    	ConnectorConfig config = new ConnectorConfig();
	    	
	    	try {
	    		config.connect("df", "https://WWWS030-TAGRAWA.int.whishworks.com:53443/OPCUA/SimulationServer");
	    		conn.setConfig(config);
	    		CustomNodeId customNodeId = new CustomNodeId();
	    		customNodeId.setType(IdType.String);
	    		customNodeId.setValue("MyDevice");
	    		customNodeId.setNamespaceIndex(2);
	    		List<UaMethod> methods = conn.getMethods(customNodeId);
	    		System.out.println(methods);
	    		Argument[] inputArguments = methods.get(0).getInputArguments();
	    		for(int i=0; i < inputArguments.length;i++){
	    			UaDataType dataType = (UaDataType) config.getClient().getAddressSpace().getType(inputArguments[i].getDataType());
	    		System.out.println((String.format("%s: %s {%s} = ", inputArguments[i].getName(), dataType.getDisplayName().getText(),
						inputArguments[i].getDescription().getText())));
	    		}
	    		System.out.println(config.getNodes());
	    		//AddressSpace space = conn.blah();
	    	//	conn.browse();
	    		//System.out.println(space);
	    	} catch (ConnectionException e) {
	    		// TODO Auto-generated catch block
	    		e.printStackTrace();
	    	}
	    }
	    
	
}