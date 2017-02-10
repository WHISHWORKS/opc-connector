package org.mule.modules.opc.entity;

import java.util.List;

import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.core.ReferenceDescription;

public class CustomAddressSpaceNode {
	
	private CustomNodeId nodeId;
	private ReferenceDescription reference;
	private CustomNodeId parentNode;
	private String referenceDefinition;
	private String nodeDefinition;
	List<CustomAddressSpaceNode> addressSpaceNodes;
	
	
	public CustomAddressSpaceNode() {
		super();
	}


	public CustomNodeId getNodeId() {
		return nodeId;
	}


	public void setNodeId(CustomNodeId nodeId) {
		this.nodeId = nodeId;
	}


	public ReferenceDescription getReference() {
		return reference;
	}


	public void setReference(ReferenceDescription reference) {
		this.reference = reference;
	}


	public CustomNodeId getParentNode() {
		return parentNode;
	}


	public void setParentNode(CustomNodeId customNodeId) {
		this.parentNode = customNodeId;
	}


	public List<CustomAddressSpaceNode> getAddressSpaceNodes() {
		return addressSpaceNodes;
	}


	public void setAddressSpaceNodes(List<CustomAddressSpaceNode> addressSpaceNodes) {
		this.addressSpaceNodes = addressSpaceNodes;
	}


	public String getReferenceDefinition() {
		return referenceDefinition;
	}


	public void setReferenceDefinition(String referenceDefinition) {
		this.referenceDefinition = referenceDefinition;
	}


	public String getNodeDefinition() {
		return nodeDefinition;
	}


	public void setNodeDefinition(String nodeDefinition) {
		this.nodeDefinition = nodeDefinition;
	}
	
	
	

}
