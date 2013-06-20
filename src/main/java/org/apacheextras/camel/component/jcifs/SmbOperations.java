/**************************************************************************************
 * jcifs Camel component
 * Copyright (C) 2010 Redpill Linpro AB
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ***************************************************************************************/
package org.apacheextras.camel.component.jcifs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.component.file.FileComponent;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileEndpoint;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.component.file.GenericFileOperations;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.FileUtil;
import org.apache.camel.util.IOHelper;
import org.apache.camel.util.ObjectHelper;

public class SmbOperations<SmbFile> implements GenericFileOperations<SmbFile> {

	private GenericFileEndpoint<SmbFile> endpoint;
	private SmbClient client;


	public SmbOperations(SmbClient smbClient) {
		this.client = smbClient;
	}

	public void setEndpoint(GenericFileEndpoint<SmbFile> endpoint) {
		this.endpoint = endpoint;
	}

	public boolean deleteFile(String name) throws GenericFileOperationFailedException {
		try {
			login();
			return client.delete(getPath(name));

		} catch (Exception e) {
			throw new GenericFileOperationFailedException("could not delete file " + e);
		}
	}

	public boolean existsFile(String name) throws GenericFileOperationFailedException {
		try {
			login();
			return client.isExist(getPath(name));
		} catch (Exception e) {
			throw new GenericFileOperationFailedException("could not determine if file exists " + e);
		}
	}

	public boolean renameFile(String from, String to) throws GenericFileOperationFailedException {
		String fromPath = getPath(from);
		String toPath = getPath(to);
		try {
			login();
			return client.rename(fromPath, toPath);
		} catch (Exception e) {
			throw new GenericFileOperationFailedException("could not rename file " + e);
		}
	}

	public boolean buildDirectory(String directory, boolean absolute) throws GenericFileOperationFailedException {
		try {
			login();
			return client.createDirs(getPath(directory));
		} catch (Exception e) {
			return false;
		}
	}

	public boolean retrieveFile(String name, Exchange exchange) throws GenericFileOperationFailedException {
		if (ObjectHelper.isNotEmpty(endpoint.getLocalWorkDirectory())) {
			return retrieveFileToFileInLocalWorkDirectory(name, exchange);
		} 
		return retrieveFileToStreamInBody(name, exchange);
	}

	@SuppressWarnings("unchecked")
	private boolean retrieveFileToFileInLocalWorkDirectory(String name, Exchange exchange) throws GenericFileOperationFailedException {
		File temp;
		
		File local = new File(endpoint.getLocalWorkDirectory());
		local.mkdirs();
		OutputStream os;
		GenericFile<SmbFile> file = (GenericFile<SmbFile>) exchange.getProperty(FileComponent.FILE_EXCHANGE_FILE);
		ObjectHelper.notNull(file, "Exchange should have the " + FileComponent.FILE_EXCHANGE_FILE + " set");
		
		// use relative filename in local work directory
		String relativeName = file.getRelativeFilePath();
		temp = new File(local, relativeName + ".inprogress");
		local = new File(local, relativeName);
			
		// delete any existing files
		if (temp.exists()) {
			if (!FileUtil.deleteFile(temp)) {
				throw new GenericFileOperationFailedException("Cannot delete existing local work file: " + temp);
			}
		}
		if (local.exists()) {
			if (!FileUtil.deleteFile(local)) {
				throw new GenericFileOperationFailedException("Cannot delete existing local work file: " + local);
			}
		}
		// create new temp local work file
		try {
			if (!temp.createNewFile()) {
				throw new GenericFileOperationFailedException("Cannot create new local work file: " + temp);
			}
		} catch (IOException e1) {
			throw new GenericFileOperationFailedException("Cannot create new local work file: " + temp + " " + e1);
		}

		// store content as a file in the local work directory in the temp handle
		try {
			os = new FileOutputStream(temp);
		} catch (FileNotFoundException e1) {
			throw new GenericFileOperationFailedException("File not found: " + temp + " " + e1);
		}

		// set header with the path to the local work file
		exchange.getIn().setHeader(Exchange.FILE_LOCAL_WORK_PATH, local.getPath());
		
		boolean result;

		try {
			// store the java.io.File handle as the body
			file.setBody(local);
			login();
			result = client.retrieveFile(getPath(name), os);
		} catch (IOException e) {
			throw new GenericFileOperationFailedException("Cannot retrieve file: " + name, e);
		} catch (Exception e) {
			throw new GenericFileOperationFailedException("Cannot retrieve file: " + name, e);
		} finally {
			IOHelper.close(os, "retrieve: " + name);
		}

		try {
			if (!FileUtil.renameFile(temp, local, true)) {
				throw new GenericFileOperationFailedException("Cannot rename local work file from: " + temp + " to: " + local);
			}
		} catch (IOException e) {
			throw new GenericFileOperationFailedException("Cannot rename local work file from: " + temp + " to: " + local, e);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private boolean retrieveFileToStreamInBody(String name, Exchange exchange) throws GenericFileOperationFailedException {
		OutputStream os = null;
		boolean result;
		try {
			os = new ByteArrayOutputStream();
			GenericFile<SmbFile> target = (GenericFile<SmbFile>) exchange.getProperty(FileComponent.FILE_EXCHANGE_FILE);
			ObjectHelper.notNull(target, "Exchange should have the " + FileComponent.FILE_EXCHANGE_FILE + " set");
			target.setBody(os);

			// use input stream which works with Apache SSHD used for testing
			login();
			result = client.retrieveFile(getPath(name), os);

		} catch (IOException e) {
			throw new GenericFileOperationFailedException("Cannot retrieve file: " + name, e);
		} catch (Exception e) {
			throw new GenericFileOperationFailedException("Cannot retrieve file: " + name, e);
		} finally {
			IOHelper.close(os, "retrieve: " + name);
		}

		return result;
	}

	public boolean storeFile(String name, Exchange exchange) throws GenericFileOperationFailedException {
		String storeName = getPath(name);

		InputStream is = null;
		try {
			is = ExchangeHelper.getMandatoryInBody(exchange, InputStream.class);

			login();
			client.storeFile(storeName, is);
			return true;
		} catch (Exception e) {
			throw new GenericFileOperationFailedException("Cannot store file " + storeName, e);
		} finally {
			IOHelper.close(is, "store: " + storeName);
		}
	}

	public String getCurrentDirectory() throws GenericFileOperationFailedException {
		return null;
	}

	public void changeCurrentDirectory(String path) throws GenericFileOperationFailedException {
	}

	public void changeToParentDirectory() throws GenericFileOperationFailedException {
	}

	public List<SmbFile> listFiles() throws GenericFileOperationFailedException {
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<SmbFile> listFiles(String path) throws GenericFileOperationFailedException {
		String listPath = getDirPath(path);
		List<SmbFile> files = new ArrayList<SmbFile>();
		try {
			login();
			for (Object f : client.listFiles(listPath)){
				files.add((SmbFile) f);
			}
		} catch (Exception e) {
			throw new GenericFileOperationFailedException("Could not get files " + e.getMessage());
		}
		return files;
	}

	public void login() {
		String domain = ((SmbConfiguration) endpoint.getConfiguration()).getDomain();
		String username = ((SmbConfiguration) endpoint.getConfiguration()).getUsername();
		String password = ((SmbConfiguration) endpoint.getConfiguration()).getPassword();
	
		client.login(domain, username, password);
	}

	private String getPath(String pathEnd) {
		String path = ((SmbConfiguration)endpoint.getConfiguration()).getSmbHostPath() + pathEnd;
		return path.replace('\\', '/');
	}

	private String getDirPath(String pathEnd) {
		String path = ((SmbConfiguration)endpoint.getConfiguration()).getSmbHostPath() + pathEnd;
		if (!path.endsWith("/")) {
			path = path + "/";
		}
		return path.replace('\\', '/');
	}

	public void releaseRetreivedFileResources(Exchange exchange)
			throws GenericFileOperationFailedException {
		// Right now do nothing		
	}
}
