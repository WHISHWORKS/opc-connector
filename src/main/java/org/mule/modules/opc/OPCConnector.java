package org.mule.modules.opc;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.mule.api.ConnectionException;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.MetaDataScope;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.display.FriendlyName;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.MetaDataKeyParam;
import org.mule.api.annotations.param.MetaDataKeyParamAffectsType;
import org.mule.api.annotations.param.Optional;
import org.mule.modules.opc.config.ConnectorConfig;
import org.mule.modules.opc.datasense.MethodResolver;
import org.mule.modules.opc.entity.CustomAddressSpaceNode;
import org.mule.modules.opc.entity.CustomNodeId;
import org.mule.modules.opc.inputenums.AttributeId;
import org.mule.modules.opc.utility.UtilMethod;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.UnsignedShort;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.Argument;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.DataChangeFilter;
import org.opcfoundation.ua.core.DataChangeTrigger;
import org.opcfoundation.ua.core.DeadbandType;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.IdType;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.MonitoringMode;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.core.ReferenceDescription;
import org.opcfoundation.ua.utils.AttributesUtil;
import org.opcfoundation.ua.utils.MultiDimensionArrayUtils;

import com.prosysopc.ua.MethodCallStatusException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.client.MonitoredDataItem;
import com.prosysopc.ua.client.MonitoredDataItemListener;
import com.prosysopc.ua.client.ServerConnectionException;
import com.prosysopc.ua.client.ServerList;
import com.prosysopc.ua.client.ServerListException;
import com.prosysopc.ua.client.Subscription;
import com.prosysopc.ua.client.SubscriptionAliveListener;
import com.prosysopc.ua.client.SubscriptionNotificationListener;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.client.nodes.UaMethodImpl;
import com.prosysopc.ua.nodes.MethodArgumentException;
import com.prosysopc.ua.nodes.UaDataType;
import com.prosysopc.ua.nodes.UaMethod;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaReferenceType;
import com.prosysopc.ua.nodes.UaType;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.samples.client.MyMonitoredDataItemListener;
import com.prosysopc.ua.samples.client.MySubscriptionAliveListener;
import com.prosysopc.ua.samples.client.MySubscriptionNotificationListener;
import org.mule.api.annotations.MetaDataKeyRetriever;
import org.mule.api.annotations.MetaDataRetriever;
import org.mule.common.metadata.MetaDataKey;
import org.mule.common.metadata.MetaData;

@MetaDataScope(MethodResolver.class)
@Connector(name="opc", friendlyName="OPC")
public class OPCConnector {

    @Config
    ConnectorConfig config;

    private UaClient client; 
    
    private CustomNodeId rootNode = UtilMethod.getCustomNodeId(Identifiers.RootFolder);
    
	protected static final int ACTION_ALL = -4;
	protected static final int ACTION_BACK = -2;
	protected static final int ACTION_RETURN = -1;
	protected static final int ACTION_ROOT = -3;
	protected static final int ACTION_TRANSLATE = -6;
	protected static final int ACTION_UP = -5;
	protected boolean showReadValueDataType = false;
	protected static boolean stackTraceOnException = false;

	protected Subscription subscription;
	protected SubscriptionAliveListener subscriptionAliveListener = new MySubscriptionAliveListener();
	protected final List<String> initialMonitoredItems = new ArrayList<String>();
	protected SubscriptionNotificationListener subscriptionListener = new MySubscriptionNotificationListener();
	protected MonitoredDataItemListener dataChangeListener = new MyMonitoredDataItemListener(null);
 

	public ConnectorConfig getConfig() {
        return config;
    }

    public void setConfig(ConnectorConfig config) {
        this.config = config;
        client = config.getClient();
    }
    
    @Processor
    public CustomNodeId getRootNode() {
		return this.rootNode;
	}
    

    
    @Processor
	public List<ReferenceDescription> getReferences(CustomNodeId customNodeId)   {
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
		/*System.out.println("-------------------------------------------------------");
		//println("- Enter node number to browse into that");
		//println("- Enter a to show/hide all references");
		if (prevId != null) {
			String prevName = null;
			try {
				UaNode prevNode = client.getAddressSpace().getNode(prevId);
				if (prevNode != null)
					prevName = prevNode.getDisplayName().getText();
			} catch (AddressSpaceException e) {
				prevName = prevId.toString();
			} catch(ServiceException e)
			{
				
			}
			if (prevName != null)
				//println("- Enter b to browse back to the previous node (" + prevName + ")");
		
		if (!upReferences.isEmpty())
			System.out.println("- Enter u to browse up to the 'parent' node");
		//println("- Enter r to browse back to the root node");
		//println("- Enter t to translate a BrowsePath to NodeId");
		System.out.println("- Enter x to select the current node and return to previous menu");
		System.out.println("-------------------------------------------------------");
		do {
			//int action = readAction();
			switch (action) {
			case ACTION_RETURN:
				return nodeId;
			case ACTION_BACK:
				if (prevId == null)
					continue;
				return prevId;
			case ACTION_UP:
				if ((!upReferences.isEmpty()))
					try {
						ReferenceDescription upReference = null;
						if (upReferences.size() == 1)
							upReference = upReferences.get(0);
						else {
							//println("Which inverse reference do you wish to go up?");
							for (int i = 0; i < upReferences.size(); i++)
								//printf("%d - %s\n", i, referenceToString(upReferences.get(i)));
							while (upReference == null) {
								//int upIndex = readAction();
								try {
									//upReference = upReferences.get(upIndex);
								} catch (Exception e) {
									//printException(e);
								}
							}
						}
						if (!upReference.getNodeId().isLocal())
							System.out.println("Not a local node");
						else
							return browse(
									client.getAddressSpace().getNamespaceTable().toNodeId(upReference.getNodeId()),
									nodeId,-7);
					} catch (ServiceResultException e1) {
						//printException(e1);
					}
			case ACTION_ROOT:
				return browse(Identifiers.RootFolder, nodeId,-7);
			case ACTION_ALL:
				if (NodeId.isNull(client.getAddressSpace().getReferenceTypeId())) {
					client.getAddressSpace().setReferenceTypeId(Identifiers.HierarchicalReferences);
					client.getAddressSpace().setBrowseDirection(BrowseDirection.Forward);
				} else {
					// request all types
					client.getAddressSpace().setReferenceTypeId(NodeId.NULL);
					client.getAddressSpace().setBrowseDirection(BrowseDirection.Both);
				}
				// if (ReferencesToReturn == null) {
				// ReferencesToReturn = Identifiers.HierarchicalReferences;
				// client.getAddressSpace().setBrowseDirection(
				// BrowseDirection.Forward);
				// } else {
				// ReferencesToReturn = null;
				// client.getAddressSpace().setBrowseDirection(
				// BrowseDirection.Both);
				// }
				return browse(nodeId, prevId,-7);
			case ACTION_TRANSLATE:
				//println("Which node do you wish to translate?");
				//println("Use / to separate nodes in the browsePath, e.g. 'Types/ObjectTypes/BaseObjectType/3:YourType'");
				//println("where each element is a 'parseable' BrowseName, i.e. the namespaceIndex can be defined with a prefix, like '3:'");
				String browsePathString = "";

				List<RelativePathElement> browsePath = new ArrayList<RelativePathElement>();
				for (String s : browsePathString.split("/")) {
					final QualifiedName targetName = QualifiedName.parseQualifiedName(s);
					browsePath
							.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, targetName));
				}
				// The result may always contain several targets (if there are
				// nodes with the same browseName), although normally only one
				// is expected
				BrowsePathTarget[] pathTargets;
				try {
					pathTargets = client.getAddressSpace().translateBrowsePathToNodeId(nodeId,
							browsePath.toArray(new RelativePathElement[0]));
					for (BrowsePathTarget pathTarget : pathTargets) {
						String targetStr = "Target: " + pathTarget.getTargetId();
						if (!pathTarget.getRemainingPathIndex().equals(UnsignedInteger.MAX_VALUE))
							targetStr = targetStr + " - RemainingPathIndex: " + pathTarget.getRemainingPathIndex();
						println(targetStr);
					}
				} catch (StatusException e1) {
					//printException(e1);
				}catch(ServiceException e)
				{
					
				}
				break;

			default:
				try {
					ReferenceDescription r = references.get(action);
					NodeId target;
					try {
						target = browse(client.getAddressSpace().getNamespaceTable().toNodeId(r.getNodeId()), nodeId,-7);
					} catch (ServiceResultException e) {
						throw new ServiceException(e);
					}
					if (target != nodeId)
						return target;
					return browse(nodeId, prevId,-7);
				} catch (IndexOutOfBoundsException e) {
					System.out.println("No such item: " + action);
				}catch(ServiceException e)
				{
					
				}
			}
		} while (true);
		}
		return prevId;
		*/
    }
    
    @Processor 
    public List<ReferenceDescription> getUpReferences(CustomNodeId customNodeodeId)   {
		//printCurrentNode(nodeId);
		// client.getAddressSpace().setReferenceTypeId(ReferencesToReturn);
		//List<ReferenceDescription> references;
		// Find the reference to use for browsing up: prefer the previous node,
		// but otherwise accept any hierarchical inverse reference
    	NodeId nodeId = CustomNodeId.get(customNodeodeId.getIdType(), customNodeodeId.getNamespaceIndex(), customNodeodeId.getValue());
		List<ReferenceDescription> upReferences = null;
		client.getAddressSpace().setMaxReferencesPerNode(1000);
			//references = client.getAddressSpace().browse(nodeId);
			/*for (int i = 0; i < references.size(); i++)
				//printf("%d - %s\n", i, referenceToString(references.get(i)));
*/		try {
			upReferences = client.getAddressSpace().browseUp(nodeId);
		} catch (ServerConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return upReferences;
		/*System.out.println("-------------------------------------------------------");
		//println("- Enter node number to browse into that");
		//println("- Enter a to show/hide all references");
		if (prevId != null) {
			String prevName = null;
			try {
				UaNode prevNode = client.getAddressSpace().getNode(prevId);
				if (prevNode != null)
					prevName = prevNode.getDisplayName().getText();
			} catch (AddressSpaceException e) {
				prevName = prevId.toString();
			} catch(ServiceException e)
			{
				
			}
			if (prevName != null)
				//println("- Enter b to browse back to the previous node (" + prevName + ")");
		
		if (!upReferences.isEmpty())
			System.out.println("- Enter u to browse up to the 'parent' node");
		//println("- Enter r to browse back to the root node");
		//println("- Enter t to translate a BrowsePath to NodeId");
		System.out.println("- Enter x to select the current node and return to previous menu");
		System.out.println("-------------------------------------------------------");
		do {
			//int action = readAction();
			switch (action) {
			case ACTION_RETURN:
				return nodeId;
			case ACTION_BACK:
				if (prevId == null)
					continue;
				return prevId;
			case ACTION_UP:
				if ((!upReferences.isEmpty()))
					try {
						ReferenceDescription upReference = null;
						if (upReferences.size() == 1)
							upReference = upReferences.get(0);
						else {
							//println("Which inverse reference do you wish to go up?");
							for (int i = 0; i < upReferences.size(); i++)
								//printf("%d - %s\n", i, referenceToString(upReferences.get(i)));
							while (upReference == null) {
								//int upIndex = readAction();
								try {
									//upReference = upReferences.get(upIndex);
								} catch (Exception e) {
									//printException(e);
								}
							}
						}
						if (!upReference.getNodeId().isLocal())
							System.out.println("Not a local node");
						else
							return browse(
									client.getAddressSpace().getNamespaceTable().toNodeId(upReference.getNodeId()),
									nodeId,-7);
					} catch (ServiceResultException e1) {
						//printException(e1);
					}
			case ACTION_ROOT:
				return browse(Identifiers.RootFolder, nodeId,-7);
			case ACTION_ALL:
				if (NodeId.isNull(client.getAddressSpace().getReferenceTypeId())) {
					client.getAddressSpace().setReferenceTypeId(Identifiers.HierarchicalReferences);
					client.getAddressSpace().setBrowseDirection(BrowseDirection.Forward);
				} else {
					// request all types
					client.getAddressSpace().setReferenceTypeId(NodeId.NULL);
					client.getAddressSpace().setBrowseDirection(BrowseDirection.Both);
				}
				// if (ReferencesToReturn == null) {
				// ReferencesToReturn = Identifiers.HierarchicalReferences;
				// client.getAddressSpace().setBrowseDirection(
				// BrowseDirection.Forward);
				// } else {
				// ReferencesToReturn = null;
				// client.getAddressSpace().setBrowseDirection(
				// BrowseDirection.Both);
				// }
				return browse(nodeId, prevId,-7);
			case ACTION_TRANSLATE:
				//println("Which node do you wish to translate?");
				//println("Use / to separate nodes in the browsePath, e.g. 'Types/ObjectTypes/BaseObjectType/3:YourType'");
				//println("where each element is a 'parseable' BrowseName, i.e. the namespaceIndex can be defined with a prefix, like '3:'");
				String browsePathString = "";

				List<RelativePathElement> browsePath = new ArrayList<RelativePathElement>();
				for (String s : browsePathString.split("/")) {
					final QualifiedName targetName = QualifiedName.parseQualifiedName(s);
					browsePath
							.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, targetName));
				}
				// The result may always contain several targets (if there are
				// nodes with the same browseName), although normally only one
				// is expected
				BrowsePathTarget[] pathTargets;
				try {
					pathTargets = client.getAddressSpace().translateBrowsePathToNodeId(nodeId,
							browsePath.toArray(new RelativePathElement[0]));
					for (BrowsePathTarget pathTarget : pathTargets) {
						String targetStr = "Target: " + pathTarget.getTargetId();
						if (!pathTarget.getRemainingPathIndex().equals(UnsignedInteger.MAX_VALUE))
							targetStr = targetStr + " - RemainingPathIndex: " + pathTarget.getRemainingPathIndex();
						println(targetStr);
					}
				} catch (StatusException e1) {
					//printException(e1);
				}catch(ServiceException e)
				{
					
				}
				break;

			default:
				try {
					ReferenceDescription r = references.get(action);
					NodeId target;
					try {
						target = browse(client.getAddressSpace().getNamespaceTable().toNodeId(r.getNodeId()), nodeId,-7);
					} catch (ServiceResultException e) {
						throw new ServiceException(e);
					}
					if (target != nodeId)
						return target;
					return browse(nodeId, prevId,-7);
				} catch (IndexOutOfBoundsException e) {
					System.out.println("No such item: " + action);
				}catch(ServiceException e)
				{
					
				}
			}
		} while (true);
		}
		return prevId;
		*/
    }
	
    @Processor
    public String read(CustomNodeId customNodeodeId,long attribute) {
    	UnsignedInteger attributeId = UnsignedInteger.valueOf(attribute);
    	NodeId nodeId = CustomNodeId.get(customNodeodeId.getIdType(), customNodeodeId.getNamespaceIndex(), customNodeodeId.getValue());
		println("read node " + nodeId);
		//UnsignedInteger attributeId = readAttributeId();
		DataValue value = null;
		try {
			value = client.readAttribute(nodeId, attributeId);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StatusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataValueToString(nodeId, attributeId, value);

	}
    
  
    @Processor
    public List<UnsignedInteger> getAttributeIds(){
    	List<UnsignedInteger> attributesIds = new LinkedList<>();
    	//println("Select the node attribute.");
		for (long i = Attributes.NodeId.getValue(); i < Attributes.UserExecutable.getValue(); i++){
			attributesIds.add(UnsignedInteger.valueOf(i));
			//printf("%d - %s\n", i, AttributesUtil.toString(UnsignedInteger.valueOf(i)));
		}//int action = readAction();
		//if (action < 0)
		//	return null;
		/*UnsignedInteger attributeId = UnsignedInteger.valueOf(AttributeId.Value.getValue());
		System.out.println("attribute: " + AttributesUtil.toString(attributeId));*/
		return attributesIds;
    }
    
/*	private boolean discover()  {
		ApplicationDescription serverApp = null;
		try{
			
			ServerList serverList = discoverServer(client.getUri());
			if(serverList!=null) serverApp = serverList.get(0);
			//serverApp = discoverServer(client.getUri());
		if (serverApp != null) {
			 List<EndpointDescription>  endpoints = discoverEndpoints(serverApp);
			if (endpoint != null) {
				client.disconnect();
				client.setEndpoint(endpoint);
				return true;
			}
		}
		}catch(ServerListException e){
			
		}catch(URISyntaxException e){
			
		}
		return false;
	}*/
	
    @Processor
	public List<EndpointDescription> discoverEndpoints(ApplicationDescription serverApp) {
		final String[] discoveryUrls = serverApp.getDiscoveryUrls();
		if (discoveryUrls != null) {
			UaClient discoveryClient = new UaClient();
			int i = 0;
			List<EndpointDescription> edList = new ArrayList<EndpointDescription>();

			println("Available endpoints: ");
			println(String.format("%s - %-50s - %-20s - %-20s - %s", "#", "URI", "Security Mode", "Security Policy",
					"Transport Profile"));
			for (String url : discoveryUrls) {
				try {
					discoveryClient.setUri(url);
				
					for (EndpointDescription ed : discoveryClient.discoverEndpoints()) {
						println(String.format("%s - %-50s - %-20s - %-20s - %s", i++, ed.getEndpointUrl(),
								ed.getSecurityMode(),
								ed.getSecurityPolicyUri().replaceFirst("http://opcfoundation.org/UA/SecurityPolicy#",
										""),
								ed.getTransportProfileUri()
										.replaceFirst("http://opcfoundation.org/UA-Profile/Transport/", "")));
						edList.add(ed);
					}
				} catch (URISyntaxException e) {
					println("Cannot discover Endpoints from URL " + url + ": " + e.getMessage());
				}catch(ServiceException ex){
					
				}
			}
			/*System.out.println("-------------------------------------------------------");
			println("- Enter endpoint number to select that one");
			println("- Enter x to return to cancel");
			System.out.println("-------------------------------------------------------");
			// // Select an endpoint with the same protocol as the
			// // original request, if available
			// URI uri = new URI(url);
			// if (uri.getScheme().equals(client.getProtocol().toString()))
			// {
			// connectUrl = url;
			// println("Selected application "
			// + serverApp.getApplicationName().getText()
			// + " at " + url);
			// break;
			// } else if (connectUrl == null)
			// connectUrl = url;

			EndpointDescription endpoint = null;
			while (endpoint == null)
				try {
					int n = -2;
					if (n == ACTION_RETURN)
						return null;
					else
						return edList.get(n);
				} catch (Exception e) {

				}
		} else
			println("No suitable discoveryUrl available: using the current Url");
		return null;*/
			return edList;
		}
		return null;
	}
	
	@Processor
	public ServerList discoverServer(String uri)  {
		// Discover a new server list from a discovery server at URI
		ServerList serverList = null;
		try {
			serverList = new ServerList(uri);
		} catch (ServerListException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (serverList==null || serverList.size() == 0) {
			//println("No servers found");
			return null;
		}

		/*println(String.format("%s - %-25s - %-15s - %-30s - %s", "#", "Name", "Type", "Product", "Application"));
		for (int i = 0; i < serverList.size(); i++) {
			final ApplicationDescription s = serverList.get(i);
			println(String.format("%d - %-25s - %-15s - %-30s - %s", i, s.getApplicationName().getText(),
					s.getApplicationType(), s.getProductUri(), s.getApplicationUri()));
		}*/
		//System.out.println("-------------------------------------------------------");
		//println("- Enter client number to select that one");
		//println("- Enter x to return to cancel");
		//System.out.println("-------------------------------------------------------");
		/*do {
			int action = -3;
			switch (action) {
			case ACTION_RETURN:
				return null;
			default:
				return serverList.get(action);
			}
		} while (true);*/
		return serverList;
	}
	
	@Processor
	public String write(CustomNodeId customNodeodeId,long attribute,String value) throws ServiceException, AddressSpaceException, StatusException{
		//UnsignedInteger attributeId = readAttributeId();
		UnsignedInteger attributeId = UnsignedInteger.valueOf(attribute);
		NodeId nodeId = CustomNodeId.get(customNodeodeId.getIdType(), customNodeodeId.getNamespaceIndex(), customNodeodeId.getValue());
		UaNode node = client.getAddressSpace().getNode(nodeId);
		println("Writing to node " + nodeId + " - " + node.getDisplayName().getText());

		// Find the DataType if setting Value - for other properties you must
		// find the correct data type yourself
		UaDataType dataType = null;
		if (attributeId.equals(Attributes.Value) && (node instanceof UaVariable)) {
			UaVariable v = (UaVariable) node;
			dataType = (UaDataType) v.getDataType();
			println("DataType: " + dataType.getDisplayName().getText());
		}

		//print("Enter the value to write: ");
		//String value = readInput(true);
		String response = "Not updated! ";
		try {
			Object convertedValue = dataType != null
					? client.getAddressSpace().getDataTypeConverter().parseVariant(value, dataType) : value;
			boolean status = client.writeAttribute(nodeId, attributeId, convertedValue);
			response = "Successfully updated! ";
			if (status){
				
				println("OK");
			}else{
				println("OK (completes asynchronously)");
				}
		} catch (ServiceException e) {
			printException(e);
		} catch (StatusException e) {
			printException(e);
		}

		return response + client.readAttribute(nodeId, attributeId);
	}
	
	
	@Processor
	public CustomAddressSpaceNode getAddressSpace() throws ServiceResultException{
		CustomAddressSpaceNode node  =  new CustomAddressSpaceNode();
		node.setNodeId(rootNode);
		node.setParentNode(null);
		node.setReference(null);
		node.setNodeDefinition(rootNode.toString());
		node.setAddressSpaceNodes(searchChildNodes(node));
		return node;
	}
	
	private List<CustomAddressSpaceNode> searchChildNodes(CustomAddressSpaceNode node) throws ServiceResultException{
		List<ReferenceDescription> references = getReferences(node.getNodeId());
		List<CustomAddressSpaceNode> childs = new LinkedList<>();
		for(ReferenceDescription r: references){
			CustomAddressSpaceNode childNode = new CustomAddressSpaceNode();
			CustomNodeId customnodeId = UtilMethod.getCustomNodeId(client.getAddressSpace().getNamespaceTable().toNodeId(r.getNodeId()));
			childNode.setNodeId(customnodeId);
			childNode.setParentNode(node.getNodeId());
			childNode.setReference(r);
			String des = referenceToString(r);
			childNode.setReferenceDefinition(des);
			childNode.setNodeDefinition(customnodeId.toString());
			childNode.setAddressSpaceNodes(searchChildNodes(childNode));
			childs.add(childNode);
		}
		return childs;
	}
	
	
	/**
	 * Comment for method
	 * @throws StatusException 
	 * @throws AddressSpaceException 
	 * @throws ServiceException 
	 * @throws MethodArgumentException 
	 * @throws ServerConnectionException 
	 */
	@MetaDataScope(MethodResolver.class)
	@Processor
	public Variant[] callMethod(@FriendlyName("NodeId") CustomNodeId nodeID,@FriendlyName("MethodId") @MetaDataKeyParam(affects = MetaDataKeyParamAffectsType.INPUT)  String methodID, @Default("#[payload]") Map<String,String> input) throws ServiceException, AddressSpaceException, StatusException, ServerConnectionException, MethodArgumentException  {
		// // Example values to call "condition acknowledge" using the standard
		// // methodId:
		// methodId = Identifiers.AcknowledgeableConditionType_Acknowledge;
		// // change this to the ID of the event you are acknowledging:
		// byte[] eventId = null;
		// LocalizedText comment = new LocalizedText("Your comment",
		// Locale.ENGLISH);
		// final Variant[] inputs = new Variant[] { new Variant(eventId),
		// new Variant(comment) };
		//NodeId methodId = CustomNodeId.get(methodID.getIdType(), methodID.getNamespaceIndex(), methodID.getValue());
		NodeId nodeId = CustomNodeId.get(nodeID.getIdType(), nodeID.getNamespaceIndex(), nodeID.getValue());
		
		//UaMethod method = client.getAddressSpace().getMethod(methodId);
		//Variant[] inputs = readInputArguments(method,input);
		//Variant[] outputs = client.call(nodeId, methodId, inputs);
		//printOutputArguments(method, outputs);
		return null;
    }
    
	@Processor
	public Argument[] getMethodArgument(CustomNodeId methodID){
		NodeId methodId = CustomNodeId.get(methodID.getIdType(), methodID.getNamespaceIndex(), methodID.getValue());
		try {
			UaMethod method = client.getAddressSpace().getMethod(methodId);
			Argument[] inputArguments = method.getInputArguments();
			return inputArguments;
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AddressSpaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StatusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(MethodArgumentException e){
			
		}
		return null;
	}
	
	
	@Processor
	public List<CustomNodeId> getChildNodes(CustomNodeId node) throws ServiceResultException{
		List<ReferenceDescription> references = getReferences(node);
		List<CustomNodeId> childs = new LinkedList<>();
		for(ReferenceDescription r: references){
			//CustomAddressSpaceNode childNode = new CustomAddressSpaceNode();
			CustomNodeId customnodeId = UtilMethod.getCustomNodeId(client.getAddressSpace().getNamespaceTable().toNodeId(r.getNodeId()));
			
			childs.add(customnodeId);
		}
		return childs;
	}
	
	@Processor 
	public List<UaMethod> getMethods(CustomNodeId customNodeId){
		NodeId nodeId = CustomNodeId.get(customNodeId.getIdType(), customNodeId.getNamespaceIndex(), customNodeId.getValue());
		//printCurrentNode(nodeId);
		// client.getAddressSpace().setReferenceTypeId(ReferencesToReturn);
		List<ReferenceDescription> references;
		// Find the reference to use for browsing up: prefer the previous node,
		// but otherwise accept any hierarchical inverse reference
		//List<ReferenceDescription> upReferences = null;
		List<UaMethod> methods = new LinkedList<>();
		try {
			client.getAddressSpace().setMaxReferencesPerNode(1000);
			references = client.getAddressSpace().browse(nodeId);
			for (int i = 0; i < references.size(); i++){
				ReferenceDescription r = references.get(i);
				if(r.getNodeClass().equals(NodeClass.Method)){
					UaMethod method = client.getAddressSpace().getMethod(r.getNodeId());
					methods.add(method);
				}
				
			}
				//printf("%d - %s\n", i, referenceToString(references.get(i)));
			//upReferences = client.getAddressSpace().browseUp(nodeId);
		} catch (Exception e) {
			//printException(e);
			references = new ArrayList<ReferenceDescription>();
			//upReferences = new ArrayList<ReferenceDescription>();
		}
		return methods;
	}
	
	private Variant[] readInputArguments(UaMethod method,List<String> input) throws ServiceException, ServerConnectionException,
	AddressSpaceException, MethodArgumentException, StatusException {
			Argument[] inputArguments = method.getInputArguments();
			if ((inputArguments == null) || (inputArguments.length == 0))
				return new Variant[0];
			Variant[] inputs = new Variant[inputArguments.length];
			println("Enter value for Inputs:");
			for (int i = 0; i < inputs.length; i++) {
				UaDataType dataType = (UaDataType) client.getAddressSpace().getType(inputArguments[i].getDataType());
				println(String.format("%s: %s {%s} = ", inputArguments[i].getName(), dataType.getDisplayName().getText(),
						inputArguments[i].getDescription().getText()));
				while (inputs[i] == null)
					try {
						inputs[i] = client.getAddressSpace().getDataTypeConverter().parseVariant(input.get(i),dataType);
					} catch (NumberFormatException e) {
						printException(e);
					}
			}
			return inputs;
	}
	
	public String referenceToString(ReferenceDescription r)  {
		if (r == null)
			return "";
		String referenceTypeStr = null;
		try {
			// Find the reference type from the NodeCache
			UaReferenceType referenceType = (UaReferenceType) client.getAddressSpace().getType(r.getReferenceTypeId());
			if ((referenceType != null) && (referenceType.getDisplayName() != null))
				if (r.getIsForward())
					referenceTypeStr = referenceType.getDisplayName().getText();
				else
					referenceTypeStr = referenceType.getInverseName().getText();
		} catch (AddressSpaceException e) {
			printException(e);
			//print(r.toString());
			referenceTypeStr = r.getReferenceTypeId().getValue().toString();
		}catch(ServiceException e){
			printException(e);
		}
		String typeStr = null;
		switch (r.getNodeClass()) {
		case Object:
		case Variable:
			try {
				// Find the type from the NodeCache
				UaNode type = client.getAddressSpace().getNode(r.getTypeDefinition());
				if (type != null)
					typeStr = type.getDisplayName().getText();
				else
					typeStr = r.getTypeDefinition().getValue().toString();
			} catch (AddressSpaceException e) {
				printException(e);
				//print("type not found: " + r.getTypeDefinition().toString());
				typeStr = r.getTypeDefinition().getValue().toString();
			}
			catch(ServiceException e){
				printException(e);
			}
			break;
		default:
			typeStr = nodeClassToStr(r.getNodeClass());
			break;
		}
		ExpandedNodeId nodeId = r.getNodeId();
		String response = "BrowseName=" + r.getBrowseName();
		return String.format("BrowseName=%s%s %s%s (ReferenceType=%s)",r.getBrowseName(), r.getIsForward() ? "--" : " [Inverse]", r.getDisplayName().getText(), ": " + typeStr,
				referenceTypeStr);
	}
	
	private String nodeClassToStr(NodeClass nodeClass) {
		return "[" + nodeClass + "]";
	}
	
    private String dataValueToString(NodeId nodeId, UnsignedInteger attributeId, DataValue value) {
		StringBuilder sb = new StringBuilder();
		sb.append("Node: ");
		sb.append(nodeId);
		sb.append(".");
		sb.append(AttributesUtil.toString(attributeId));
		sb.append(" | Status: ");
		sb.append(value.getStatusCode());
		if (value.getStatusCode().isNotBad()) {
			sb.append(" | Value: ");
			if (value.isNull())
				sb.append("NULL");
			else {
				if (showReadValueDataType && Attributes.Value.equals(attributeId))
					try {
						UaVariable variable = (UaVariable) client.getAddressSpace().getNode(nodeId);
						if (variable == null)
							sb.append("(Cannot read node datatype from the server) ");
						else {

							NodeId dataTypeId = variable.getDataTypeId();
							UaType dataType = variable.getDataType();
							if (dataType == null)
								dataType = client.getAddressSpace().getType(dataTypeId);

							Variant variant = value.getValue();
							variant.getCompositeClass();
							if (attributeId.equals(Attributes.Value))
								if (dataType != null)
									sb.append("(" + dataType.getDisplayName().getText() + ")");
								else
									sb.append("(DataTypeId: " + dataTypeId + ")");
						}
					} catch (ServiceException e) {
					} catch (AddressSpaceException e) {
					}
				final Object v = value.getValue().getValue();
				if (value.getValue().isArray())
					sb.append(MultiDimensionArrayUtils.toString(v));
				else
					sb.append(v);
			}
		}
		sb.append(dateTimeToString(" | ServerTimestamp: ", value.getServerTimestamp(), value.getServerPicoseconds()));
		sb.append(dateTimeToString(" | SourceTimestamp: ", value.getSourceTimestamp(), value.getSourcePicoseconds()));
		return sb.toString();
	}
    
    private static String dateTimeToString(String title, DateTime timestamp, UnsignedShort picoSeconds) {
		if ((timestamp != null) && !timestamp.equals(DateTime.MIN_VALUE)) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy MMM dd (zzz) HH:mm:ss.SSS");
			StringBuilder sb = new StringBuilder(title);
			sb.append(format.format(timestamp.getCalendar(TimeZone.getDefault()).getTime()));
			if ((picoSeconds != null) && !picoSeconds.equals(UnsignedShort.valueOf(0)))
				sb.append(String.format("/%d picos", picoSeconds.getValue()));
			return sb.toString();
		}
		return "";
	}
    
    private static void println(String string) {
		System.out.println(string);
	}
    
    private static void printf(String format, Object... args) {
		System.out.printf(format, args);

	}
    
    private UnsignedInteger readAttributeId() {

		println("Select the node attribute.");
		for (long i = Attributes.NodeId.getValue(); i < Attributes.UserExecutable.getValue(); i++)
			printf("%d - %s\n", i, AttributesUtil.toString(UnsignedInteger.valueOf(i)));
		//int action = readAction();
		//if (action < 0)
		//	return null;
		UnsignedInteger attributeId = UnsignedInteger.valueOf(AttributeId.Value.getValue());
		System.out.println("attribute: " + AttributesUtil.toString(attributeId));
		return attributeId;
	}
    
	
	
	
	protected MonitoredDataItem createMonitoredDataItem(NodeId nodeId, UnsignedInteger attributeId) {
		/*
		 * Creating MonitoredDataItem, could also use the constructor without
		 * the sampling interval parameter, i.e. it would be default -1 and use
		 * the publishing inteval of the subscription, but CTT expects positive
		 * values here for some tests.
		 */
		MonitoredDataItem dataItem = new MonitoredDataItem(nodeId, attributeId, MonitoringMode.Reporting,
				subscription.getPublishingInterval());

		dataItem.setDataChangeListener(dataChangeListener);
		DataChangeFilter filter = new DataChangeFilter();
		filter.setDeadbandValue(1.00);
		filter.setTrigger(DataChangeTrigger.StatusValue);
		filter.setDeadbandType(UnsignedInteger.valueOf(DeadbandType.Percent.getValue()));
		return dataItem;
	}
	
	private  Subscription createSubscription() throws ServiceException, StatusException {
		// Create the subscription
		Subscription subscription = new Subscription();

		// Default PublishingInterval is 1000 ms

		// subscription.setPublishingInterval(1000);

		// LifetimeCount should be at least 3 times KeepAliveCount

		// subscription.setLifetimeCount(1000);
		// subscription.setMaxKeepAliveCount(50);

		// If you are expecting big data changes, it may be better to break the
		// notifications to smaller parts

		// subscription.setMaxNotificationsPerPublish(1000);

		// Listen to the alive and timeout events of the subscription

		subscription.addAliveListener(subscriptionAliveListener);

		// Listen to notifications - the data changes and events are
		// handled using the item listeners (see below), but in many
		// occasions, it may be best to use the subscription
		// listener also to handle those notifications

		subscription.addNotificationListener(subscriptionListener);

		// Add it to the client
		client.addSubscription(subscription);
		return subscription;
	}
    
	protected static void printException(Exception e) {
		if (stackTraceOnException)
			e.printStackTrace();
		else {
			println(e.toString());
			if (e instanceof MethodCallStatusException) {
				MethodCallStatusException me = (MethodCallStatusException) e;
				final StatusCode[] results = me.getInputArgumentResults();
				if (results != null)
					for (int i = 0; i < results.length; i++) {
						StatusCode s = results[i];
						if (s.isBad()) {
							println("Status for Input #" + i + ": " + s);
							DiagnosticInfo d = me.getInputArgumentDiagnosticInfos()[i];
							if (d != null)
								println("  DiagnosticInfo:" + i + ": " + d);
						}
					}
			}
			if (e.getCause() != null)
				println("Caused by: " + e.getCause());
		}
	}
	
    public static void main(String[] ar) throws ConnectionException, ServiceResultException{
    	OPCConnector conn = new OPCConnector();
    	ConnectorConfig config = conn.getConfig();
    	config.connect("wdf", "https://WWWS030-TAGRAWA.int.whishworks.com:53443/OPCUA/SimulationServer");
    	conn.setConfig(config);
    	conn.readAttributeId();
    	conn.getReferences(UtilMethod.getCustomNodeId(Identifiers.RootFolder));
    	//conn.getRootNode();
    	ServerList serverList = conn.discoverServer("https://WWWS030-TAGRAWA.int.whishworks.com:53443/OPCUA/SimulationServer");
    	ApplicationDescription appdes = null;
    	if(serverList!=null)appdes = serverList.get(0);
    	if(appdes!=null) conn.discoverEndpoints(appdes);
    	System.out.println(conn.getRootNode());
    	CustomNodeId nodeId = new CustomNodeId();
    	nodeId.setNamespaceIndex(2);
    	nodeId.setType(IdType.String);
    	nodeId.setValue("MyEnumObject");
    	CustomAddressSpaceNode node = conn.getAddressSpace();
    	CustomNodeId tmpId = node.getAddressSpaceNodes().get(0).getAddressSpaceNodes().get(1).getAddressSpaceNodes().get(0).getAddressSpaceNodes().get(0).getNodeId();
    	System.out.println(conn.getReferences(tmpId));
    	System.out.println(tmpId.compareTo(nodeId));
    	System.out.println(tmpId);
    	System.out.println(conn.getReferences(nodeId));
    }
    
}