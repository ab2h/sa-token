package cn.dev33.satoken.session;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import cn.dev33.satoken.SaTokenManager;


/**
 * session会话 
 * @author kong
 *
 */
public class SaSession implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** 此会话的id */
	private String id;	
	
	/** 此会话的创建时间 */
	private long createTime;
	
	/** 此会话的所有数据  */
	private Map<String, Object> dataMap = new ConcurrentHashMap<String, Object>();

	/**
	 * 构建一个 session对象 
	 */
 	public SaSession() {}
	
	/**
	 * 构建一个 session对象 
	 * @param id session的id 
	 */
 	public SaSession(String id) {
		this.id = id;
		this.createTime = System.currentTimeMillis(); 
	}
	
 	/**
 	 * 获取此会话id 
 	 * @return 此会话的id
 	 */
	public String getId() {
		return id;
	}

	/**
	 * 返回当前会话创建时间 
	 * @return 时间戳
	 */
	public long getCreateTime() {
		return createTime;
	}

	
	// ----------------------- tokenSign相关 
	
	/**
	 * 本session绑定的token签名列表 
	 */
	private List<TokenSign> tokenSignList = new Vector<TokenSign>();
	
	/**
	 * 返回token签名列表
	 * @return token签名列表
	 */
	public List<TokenSign> getTokenSignList() {
		return new Vector<>(tokenSignList);
	}
	
	/**
	 * 查找一个token签名 
	 * @param tokenValue token值 
	 * @return 查找到的tokenSign
	 */
	public TokenSign getTokenSign(String tokenValue) {
		for (TokenSign tokenSign : getTokenSignList()) {
 			if(tokenSign.getValue().equals(tokenValue)){
 				return tokenSign;
 			}
		}
		return null;
	}
	/**
	 * 添加一个token签名
	 * @param tokenSign token签名
	 */
	public void addTokenSign(TokenSign tokenSign) {
		// 如果已经存在于列表中，则无需再次添加 
		for (TokenSign tokenSign2 : getTokenSignList()) {
 			if(tokenSign2.getValue().equals(tokenSign.getValue())){
 				return;
 			}
		}
		// 添加并更新 
		tokenSignList.add(tokenSign);
		update();
	}
	/**
	 * 移除一个token签名
	 * @param tokenValue token名称 
	 */
	public void removeTokenSign(String tokenValue) {
		TokenSign tokenSign = getTokenSign(tokenValue);
		if(tokenSignList.remove(tokenSign)) {
			update();
		}
	}
	
	
	
	
	
	// ----------------------- 存取值 
	
	/**
	 * 写入一个值 
	 * @param key 名称
	 * @param value 值 
	 */
	public void setAttribute(String key, Object value) {
		dataMap.put(key, value);
		update();
	}
	
	/**
	 * 取出一个值 
	 * @param key 名称
	 * @return 值 
	 */
	public Object getAttribute(String key) {
		return dataMap.get(key);
	}
	
	/**
	 *  取值，并指定取不到值时的默认值 
	 * @param key 名称
	 * @param defaultValue 取不到值的时候返回的默认值 
	 * @return value
	 */
	public Object getAttribute(String key, Object defaultValue) {
		Object value = getAttribute(key);
		if(value != null) {
			return value;
		}
		return defaultValue;
	}

	/**
	 * 移除一个值 
	 * @param key 要移除的值的名字
	 */
	public void removeAttribute(String key) {
		dataMap.remove(key);
		update();
	}
	
	/**
	 * 清空所有值 
	 */
	public void clearAttribute() {
		dataMap.clear();
		update();
	}
	
	/**
	 * 是否含有指定key 
	 * @param key 是否含有指定值 
	 * @return 是否含有 
	 */
	public boolean containsAttribute(String key)  {
		return dataMap.keySet().contains(key);
	}
	
	/**
	 * 返回当前session会话所有key 
	 * @return 所有值的key列表  
	 */
	public Set<String> attributeKeys() {
		return dataMap.keySet();
	}

	/**
	 * 获取数据集合（如果更新map里的值，请调用session.update()方法避免数据过时 ）
	 * @return 返回底层储存值的map对象  
	 */
	public Map<String, Object> getDataMap() {
		return dataMap;
	}
	
	
	
	// ----------------------- 一些操作 
	
	/**
	 * 将这个session从持久库更新一下  
	 */
	public void update() {
		SaTokenManager.getSaTokenDao().updateSession(this);
	}

	/** 注销会话(注销后，此session会话将不再存储服务器上) */
	public void logout() {
		SaTokenManager.getSaTokenDao().deleteSession(this.id);
	}

	/** 如果这个token的tokenSign数量为零，则直接注销会话 */
	public void logoutByTokenSignCountToZero() {
		if(tokenSignList.size() == 0) {
			logout();
		}
	}
	
	
}
