package org.mule.modules.opc.entity;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.NamespaceTable;
import org.opcfoundation.ua.core.IdType;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.utils.CryptoUtil;

public class CustomNodeId  implements Comparable<CustomNodeId>{
	
	public static final NodeId ZERO = new NodeId(0, UnsignedInteger.getFromBits(0));
	  public static final NodeId NULL_NUMERIC = new NodeId(0, UnsignedInteger.getFromBits(0));
	  public static final NodeId NULL_STRING = get(IdType.String, 0, "");
	  public static final NodeId NULL_GUID = get(IdType.Guid, 0, new UUID(0L, 0L));
	  public static final NodeId NULL_OPAQUE = get(IdType.Opaque, 0, new byte[0]);
	  public static final NodeId NULL = NULL_NUMERIC;
	  public static final NodeId ID = Identifiers.NodeId;
	  private IdType type;
	  private int namespaceIndex;
	  private Object value;
	  static final Pattern INT_INT = Pattern.compile("ns=(\\d*);i=(\\d*)");
	  static final Pattern NONE_INT = Pattern.compile("i=(\\d*)");
	  static final Pattern INT_STRING = Pattern.compile("ns=(\\d*);s=(.*)");
	  static final Pattern NONE_STRING = Pattern.compile("s=(.*)");
	  static final Pattern INT_GUID = Pattern.compile("ns=(\\d*);g=([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");
	  static final Pattern NONE_GUID = Pattern.compile("g=([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");
	  static final Pattern INT_OPAQUE = Pattern.compile("ns=(\\d*);b=([0-9a-zA-Z\\+/=]*)");
	  static final Pattern NONE_OPAQUE = Pattern.compile("b=([0-9a-zA-Z\\+/=]*)");
	  
	  
	  
	  
	  public CustomNodeId() {
		super();
	}
	  
	  




	public static NodeId get(IdType paramIdType, int paramInt, Object paramObject)
	  {
	    if (paramIdType == IdType.Guid) {
	      return new NodeId(paramInt, (UUID)paramObject);
	    }
	    if (paramIdType == IdType.Numeric) {
	      return new NodeId(paramInt, (UnsignedInteger)paramObject);
	    }
	    if (paramIdType == IdType.Opaque) {
	      return new NodeId(paramInt, (byte[])paramObject);
	    }
	    if (paramIdType == IdType.String) {
	      return new NodeId(paramInt, (String)paramObject);
	    }
	    throw new IllegalArgumentException("bad type");
	  }
	
	
	  
	  public CustomNodeId(int paramInt1, int paramInt2)
	  {
	    this(paramInt1, UnsignedInteger.getFromBits(paramInt2));
	  }
	  
	  public CustomNodeId(int paramInt, UnsignedInteger paramUnsignedInteger)
	  {
	    if (paramUnsignedInteger == null) {
	      throw new IllegalArgumentException("Numeric NodeId cannot be null");
	    }
	    if ((paramInt < 0) || (paramInt > 65535)) {
	      throw new IllegalArgumentException("namespaceIndex out of bounds");
	    }
	    value = paramUnsignedInteger;
	    namespaceIndex = paramInt;
	    type = IdType.Numeric;
	  }
	  
	  public CustomNodeId(int paramInt, String paramString)
	  {
	    if ((paramInt < 0) || (paramInt > 65535)) {
	      throw new IllegalArgumentException("namespaceIndex out of bounds");
	    }
	    if ((paramString != null) && (paramString.length() > 4096)) {
	      throw new IllegalArgumentException("The length is restricted to 4096 characters");
	    }
	    type = IdType.String;
	    value = paramString;
	    namespaceIndex = paramInt;
	  }
	  
	  public CustomNodeId(int paramInt, UUID paramUUID)
	  {
	    if ((paramInt < 0) || (paramInt > 65535)) {
	      throw new IllegalArgumentException("namespaceIndex out of bounds");
	    }
	    if (paramUUID == null) {
	      throw new IllegalArgumentException("Numeric NodeId cannot be null");
	    }
	    type = IdType.Guid;
	    value = paramUUID;
	    namespaceIndex = paramInt;
	  }
	  
	  public CustomNodeId(int paramInt, byte[] paramArrayOfByte)
	  {
	    if ((paramInt < 0) || (paramInt > 65535)) {
	      throw new IllegalArgumentException("namespaceIndex out of bounds");
	    }
	    if ((paramArrayOfByte != null) && (paramArrayOfByte.length > 4096)) {
	      throw new IllegalArgumentException("The length is restricted to 4096 bytes");
	    }
	    type = IdType.Opaque;
	    value = paramArrayOfByte;
	    namespaceIndex = paramInt;
	  }
	  
	  public boolean isNullNodeId()
	  {
	    if (value == null) {
	      return true;
	    }
	    if (namespaceIndex != 0) {
	      return false;
	    }
	    switch (type)
	    {
	    case Numeric: 
	      return ((UnsignedInteger)value).intValue() == 0;
	    case String: 
	      return ((String)value).length() == 0;
	    case Guid: 
	      return value.equals(null);
	    case Opaque: 
	      return Arrays.equals((byte[])value, (byte[])null);
	    }
	    return false;
	  }
	  
	  public static boolean isNull(CustomNodeId customNodeId)
	  {
	    return (customNodeId == null) || (customNodeId.isNullNodeId());
	  }
	  
	  public IdType getIdType()
	  {
	    return type;
	  }
	  
	  public int getNamespaceIndex()
	  {
	    return namespaceIndex;
	  }
	  
	  public Object getValue()
	  {
	    return value;
	  }
	  
	  public void setValue(Object value){
		  this.value = value;
	  }
	  
	  
	  public IdType getType() {
		return type;
	  }

	  public void setType(IdType type) {
		  this.type = type;
	  }

	  public void setNamespaceIndex(int namespaceIndex) {
		  this.namespaceIndex = namespaceIndex;
	  }

	public int hashCode()
	  {
	    int i = 13 * namespaceIndex;
	    if (value != null) {
	      if ((value instanceof byte[])) {
	        i += 3 * Arrays.hashCode((byte[])value);
	      } else {
	        i += 3 * value.hashCode();
	      }
	    }
	    return i;
	  }
	  
	  public boolean equals(Object paramObject)
	  {
	    if (this == paramObject) {
	      return true;
	    }
	    if (paramObject == null) {
	      return isNull(this);
	    }
	 
	    if ((paramObject instanceof NodeId))
	    {
	    	CustomNodeId localObject;
	      localObject = (CustomNodeId)paramObject;
	      if ((isNull(this)) || (isNull((CustomNodeId)localObject))) {
	        return isNull(this) == isNull((CustomNodeId)localObject);
	      }
	      if ((namespaceIndex != localObject.getNamespaceIndex()) || (type != localObject.getIdType())) {
	        return false;
	      }
	      if (value == localObject.getValue()) {
	        return true;
	      }
	      if (type == IdType.Opaque) {
	        return Arrays.equals((byte[])value, (byte[])value);
	      }
	      return value.equals(value);
	    }
	    if ((paramObject instanceof ExpandedNodeId))
	    {
	    	ExpandedNodeId localObject;
	      localObject = (ExpandedNodeId)paramObject;
	      if (((localObject.getNamespaceUri() != null) && (localObject.getNamespaceUri() != NamespaceTable.OPCUA_NAMESPACE)) || (!((ExpandedNodeId)localObject).isLocal())) {
	        return false;
	      }
	      if ((namespaceIndex != localObject.getNamespaceIndex()) || (type != localObject.getIdType())) {
	        return false;
	      }
	      if (value == localObject.getValue()) {
	        return true;
	      }
	      if (type == IdType.Opaque) {
	        return Arrays.equals((byte[])value, (byte[])localObject.getValue());
	      }
	      return value.equals(value);
	    }
	    return false;
	  }
	  
	  public String toString()
	  {
	    String str = namespaceIndex > 0 ? "ns=" + namespaceIndex + ";" : "";
	    if (type == IdType.Numeric) {
	      return str + "i=" + value;
	    }
	    if (type == IdType.String) {
	      return str + "s=" + value;
	    }
	    if (type == IdType.Guid) {
	      return str + "g=" + value;
	    }
	    if (type == IdType.Opaque)
	    {
	      if (value == null) {
	        return str + "b=null";
	      }
	      return str + "b=" + new String(CryptoUtil.base64Encode((byte[])value));
	    }
	    return "error";
	  }
	  
	  @Deprecated
	  public static NodeId decode(String paramString)
	    throws IllegalArgumentException
	  {
	    return parseNodeId(paramString);
	  }
	  
	  public static NodeId parseNodeId(String paramString)
	    throws IllegalArgumentException
	  {
	    if (paramString == null) {
	      throw new IllegalArgumentException("null arg");
	    }
	    Matcher localMatcher = NONE_STRING.matcher(paramString);
	    Object localObject1;
	    if (localMatcher.matches())
	    {
	      localObject1 = localMatcher.group(1);
	      return new NodeId(0, (String)localObject1);
	    }
	    localMatcher = NONE_INT.matcher(paramString);
	    if (localMatcher.matches())
	    {
	      localObject1 = Integer.valueOf(localMatcher.group(1));
	      return new NodeId(0, ((Integer)localObject1).intValue());
	    }
	    localMatcher = NONE_GUID.matcher(paramString);
	    if (localMatcher.matches())
	    {
	      localObject1 = UUID.fromString(localMatcher.group(1));
	      return new NodeId(0, (UUID)localObject1);
	    }
	    localMatcher = NONE_OPAQUE.matcher(paramString);
	    if (localMatcher.matches())
	    {
	      localObject1 = CryptoUtil.base64Decode(localMatcher.group(1));
	      return new NodeId(0, (byte[])localObject1);
	    }
	    localMatcher = INT_INT.matcher(paramString);
	    Object localObject2;
	    if (localMatcher.matches())
	    {
	      localObject1 = Integer.valueOf(localMatcher.group(1));
	      localObject2 = Integer.valueOf(localMatcher.group(2));
	      return new NodeId(((Integer)localObject1).intValue(), ((Integer)localObject2).intValue());
	    }
	    localMatcher = INT_STRING.matcher(paramString);
	    if (localMatcher.matches())
	    {
	      localObject1 = Integer.valueOf(localMatcher.group(1));
	      localObject2 = localMatcher.group(2);
	      return new NodeId(((Integer)localObject1).intValue(), (String)localObject2);
	    }
	    localMatcher = INT_GUID.matcher(paramString);
	    if (localMatcher.matches())
	    {
	      localObject1 = Integer.valueOf(localMatcher.group(1));
	      localObject2 = UUID.fromString(localMatcher.group(2));
	      return new NodeId(((Integer)localObject1).intValue(), (UUID)localObject2);
	    }
	    localMatcher = INT_OPAQUE.matcher(paramString);
	    if (localMatcher.matches())
	    {
	      localObject1 = Integer.valueOf(localMatcher.group(1));
	      localObject2 = CryptoUtil.base64Decode(localMatcher.group(2));
	      return new NodeId(((Integer)localObject1).intValue(), (byte[])localObject2);
	    }
	    throw new IllegalArgumentException("Invalid string representation of a nodeId: " + paramString);
	  }
	  
	  public static NodeId randomGUID(int paramInt)
	  {
	    return new NodeId(paramInt, UUID.randomUUID());
	  }
	  
	  public static boolean equals(NodeId paramNodeId1, NodeId paramNodeId2)
	  {
	    if ((paramNodeId1 == null) && (paramNodeId2 != null)) {
	      return false;
	    }
	    return paramNodeId1.equals(paramNodeId2);
	  }

	@Override
	public int compareTo(CustomNodeId o) {
		int i = namespaceIndex - namespaceIndex;
		Object obj = o.getValue();
	    if (i == 0) {
	      i = type.getValue() - type.getValue();
	    }
	    if (i == 0) {
	      switch (type)
	      {
	      case Numeric: 
	        i = ((UnsignedInteger)value).compareTo((UnsignedInteger)obj);
	        break;
	      case String: 
	        i = ((String)value).compareTo((String)obj);
	        break;
	      case Guid: 
	        i = ((UUID)value).compareTo((UUID)obj);
	        break;
	      case Opaque: 
	        i = Arrays.equals((byte[])value, (byte[])obj) ? 0 : 1;
	      }
	    }
	    return i;
	}
	

}
