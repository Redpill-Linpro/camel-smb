package com.redpill_linpro.component.smb;

import jcifs.smb.SmbFile;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileEndpoint;
import org.apache.camel.component.file.GenericFileProducer;
import org.apache.camel.impl.DefaultExchange;

public class SmbEndpoint extends GenericFileEndpoint<SmbFile> {

	public SmbEndpoint(String uri, SmbComponent smbComponent, SmbConfiguration configuration) {
		super(uri, smbComponent);
		this.configuration = configuration;
	}

	@Override
	public SmbConfiguration getConfiguration() {
		return (SmbConfiguration) configuration;
	}
	
	@Override
	public SmbConsumer createConsumer(Processor processor) throws Exception {
		SmbConsumer consumer = new SmbConsumer(this, processor, createSmbOperations());
		
		consumer.setMaxMessagesPerPoll(getMaxMessagesPerPoll());
		configureConsumer(consumer);
		return consumer;
	}

	@Override
	public GenericFileProducer<SmbFile> createProducer() throws Exception {
		return new SmbProducer(this, createSmbOperations());
	}

	@Override
	public Exchange createExchange(GenericFile<SmbFile> file) {
		Exchange answer = new DefaultExchange(this);
		if (file != null) {
			file.bindToExchange(answer);
		}
		return answer;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SmbOperations<SmbFile> createSmbOperations(){
		SmbClient client = new SmbClient();
		if ( ((SmbConfiguration)this.configuration).getSmbApiFactory() != null ) {
			client.setSmbApiFactory(((SmbConfiguration)this.configuration).getSmbApiFactory());
		}
		SmbOperations operations = new SmbOperations(client);
		operations.setEndpoint(this);
		return operations;
	}

	@Override
	public String getScheme() {
		return "smb";
	}

	@Override
	public char getFileSeparator() {
		return '/';
	}

	@Override
	public boolean isAbsolute(String name) {
		return true;
	}
	
	@Override
	public boolean isSingleton(){
		return false;
	}	
}
