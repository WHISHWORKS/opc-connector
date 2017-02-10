package org.mule.modules.opc.entity;

import org.opcfoundation.ua.core.ReferenceDescription;

public class NodeIdMetaData {
	
	private ReferenceDescription referenceDescription;
	private CustomNodeId nodeId;
	
	
	
	
	public NodeIdMetaData() {
		super();
	}


	public NodeIdMetaData(ReferenceDescription referenceDescription, CustomNodeId nodeId) {
		super();
		this.referenceDescription = referenceDescription;
		this.nodeId = nodeId;
	}
	
	
	public ReferenceDescription getReferenceDescription() {
		return referenceDescription;
	}
	public void setReferenceDescription(ReferenceDescription referenceDescription) {
		this.referenceDescription = referenceDescription;
	}
	public CustomNodeId getNodeId() {
		return nodeId;
	}
	public void setNodeId(CustomNodeId nodeId) {
		this.nodeId = nodeId;
	}


	@Override
	public String toString() {
		return "[referenceDescription=" + referenceDescription + ", nodeId=" + nodeId + "]";
	}
	
	

}
