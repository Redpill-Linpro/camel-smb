package com.redpill_linpro.component.smb;

import org.apache.camel.CamelContext;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;

import com.redpill_linpro.component.smb.test.StubFileSmbApiFactory;

public abstract class BaseSmbTestSupport extends CamelTestSupport {

	protected StubFileSmbApiFactory smbApiFactory;
	
	protected abstract void setUpFileSystem() throws Exception;
	
	@Before
	public void setUp() throws Exception {
		smbApiFactory = new StubFileSmbApiFactory();
		setUpFileSystem();
		super.setUp();
	}
	
	@Override
	protected CamelContext createCamelContext() throws Exception {
		CamelContext context = super.createCamelContext();
		SmbComponent component = (SmbComponent) context.getComponent("smb");
		component.setSmbApiFactoryClass(smbApiFactory);
		return context;
	}
	
	public String getUsername() {
		return "joed";
	}
	
	public String getDomain() {
		return "WORKGROUP";
	}
	
	public String getPassword() {
		return "secret123";
	}
	
	public String getShare() {
		return "share";
	}
	
	public String getLocalSharePath() {
		return "src/test/data";
	}
}
