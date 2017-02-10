package org.mule.modules.opc.datasense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.mule.api.annotations.MetaDataKeyRetriever;
import org.mule.api.annotations.MetaDataRetriever;
import org.mule.api.annotations.components.MetaDataCategory;
import org.mule.common.metadata.DefaultMetaData;
import org.mule.common.metadata.DefaultMetaDataKey;
import org.mule.common.metadata.MetaData;
import org.mule.common.metadata.MetaDataKey;
import org.mule.common.metadata.MetaDataModel;
import org.mule.common.metadata.builder.DefaultMetaDataBuilder;
import org.mule.common.metadata.builder.DynamicObjectBuilder;
import org.mule.modules.opc.OPCConnector;
import org.mule.modules.opc.entity.CustomNodeId;
import org.mule.modules.opc.entity.NodeIdMetaData;
import org.mule.modules.opc.utility.UtilMethod;
import org.opcfoundation.ua.core.ReferenceDescription;
import org.springframework.context.annotation.Description;

@MetaDataCategory
public class MethodResolver {

	 @Inject
	 private OPCConnector connector;
	 
	 

	public OPCConnector getConnector() {
		return connector;
	}

	public void setConnector(OPCConnector connector) {
		this.connector = connector;
	}
	
	Map<String, CustomNodeId> map = new HashMap<>();

	/**
	 * Retrieves the list of keys
	 * 
	 */
	@MetaDataKeyRetriever
	public List<MetaDataKey> getMetaDataKeys() throws Exception {
		List<MetaDataKey> keys = new ArrayList<MetaDataKey>();
	
	    List<NodeIdMetaData> entities = connector.getConfig().getMethods();
	    // Generate the keys
	    for (NodeIdMetaData entity : entities) {
	    	ReferenceDescription reference  = entity.getReferenceDescription();
	    	CustomNodeId cNodeId = UtilMethod.expandedNodeIdToCustomNodeId(reference.getNodeId());
	    	String key = reference + cNodeId.toString();
	    	map.put(key, cNodeId);
	        keys.add(new DefaultMetaDataKey(key, connector.referenceToString(reference)));
	    }

	    return keys;
	}

	/**
	 * Get MetaData given a key
	 */
	@MetaDataRetriever
	public MetaData getMetaData(MetaDataKey key) throws Exception {
		//NodeId nodeId = (NodeId) key;
		
		DefaultMetaDataBuilder builder = new DefaultMetaDataBuilder();
	    // Since our model is static and we can simply create the pojo model.
	    String[] keyParts = key.getId().split("#");
	    String id = key.getId();
	    if (keyParts.length != 2) {
	        throw new RuntimeException(
	                "Invalid key. Format should be 'entityType#id'");
	    }
	    //Integer id = Integer.valueOf(keyParts[1]);
	    CustomNodeId  methodID = map.get(id);
	   /* entity.setId(id);*/
	    Description description = null;

	    /*DynamicObjectBuilder<?> dynamicObject = builder.createDynamicObject(key
	            .getId());

	    for (Description fields : description.getInnerFields()) {
	        addFields(fields, dynamicObject);
	    }*/

	    MetaDataModel model = builder.build();
	    MetaData metaData = new DefaultMetaData(model);

	    return metaData;
	}

}
