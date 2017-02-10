package org.mule.modules.opc.utility;

import org.mule.modules.opc.entity.CustomNodeId;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.NodeId;

public class UtilMethod {

	
	private UtilMethod(){
		super();
	}
	
	public static CustomNodeId getCustomNodeId(NodeId nodeId){
		CustomNodeId customNodeId = new CustomNodeId();
		customNodeId.setNamespaceIndex(nodeId.getNamespaceIndex());
		customNodeId.setType(nodeId.getIdType());
		customNodeId.setValue(nodeId.getValue());
		return customNodeId;
	}
	
	public static CustomNodeId expandedNodeIdToCustomNodeId(ExpandedNodeId expNodeId){
		CustomNodeId customNodeId = new CustomNodeId();
		customNodeId.setNamespaceIndex(expNodeId.getNamespaceIndex());
		customNodeId.setType(expNodeId.getIdType());
		customNodeId.setValue(expNodeId.getValue());
		return null;
	}
	
}
