package bradesco;

public class AccessToken {
	private String accessToken="";
	private String tokenType="";
	private long expiresIn=0;
	private String refreshToken="";
	private String scope="";
	private boolean active=false;
	
	public AccessToken()
	{
		
	}
	
	public AccessToken(String accessToken, String tokenType, long expiresIn, String refreshToken, String scope, boolean active)
	{
		this.accessToken=accessToken;
		this.tokenType=tokenType;
		this.expiresIn=expiresIn;
		this.refreshToken=refreshToken;
		this.scope=scope;
		this.active=active;
	}
	
	public void show()
	{
		System.out.println("AccessToken: "+this.accessToken);
		System.out.println("TokenType: "+this.tokenType);
		System.out.println("ExpiresIn: "+this.expiresIn);
		System.out.println("RefreshToken: "+this.refreshToken);
		System.out.println("Scope: "+this.scope);
		System.out.println("Active: "+this.active);
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getTokenType() {
		return tokenType;
	}
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
	public long getExpiresIn() {
		return expiresIn;
	}
	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	
	
}
