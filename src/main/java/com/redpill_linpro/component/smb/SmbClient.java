package com.redpill_linpro.component.smb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

import org.apache.camel.util.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  
 */
public class SmbClient {

	private NtlmPasswordAuthentication authentication;

	protected final transient Logger log = LoggerFactory.getLogger(getClass());

	/** 
	 * Creates the internal NtlmPasswordAuthentication, that is used for authentication, from the provided credentials. 
	 * 
	 * @param domain User domain to use at login
	 * @param username User name to use at login
	 * @param password The password for the provided user
	 */
	public void login(String domain, String username, String password) {
		if (log.isDebugEnabled()) {
			log.debug("login() domain[" + domain + "] username[" + username + "] password[***]");
		}
		setAuthentication(new NtlmPasswordAuthentication(domain, username, password));
	}

	/**
	 * 
	 * @param url
	 * @param out
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public boolean retrieveFile(String url, OutputStream out) throws IOException, MalformedURLException {
		if (log.isDebugEnabled()) {
			log.debug("retrieveFile() path[" + url + "]");
	    }
		SmbFile smbFile;
		smbFile = new SmbFile(url, authentication);
		IOHelper.copyAndCloseInput(smbFile.getInputStream(), out);
		return true;
	}

	public boolean createDirs(String url) {
		if (log.isDebugEnabled()) {
			log.debug("createDirs() path[" + url + "]");
		}
		SmbFile smbFile;
		try {
			smbFile = new SmbFile(url, authentication);
			if (!smbFile.exists()) {
				smbFile.mkdirs();
			}
		} catch (MalformedURLException e) {
			return false;
		} catch (SmbException e) {
			return false;
		}
		return true;

	}

	public InputStream getInputStream(String url) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("getInputStream() path[" + url + "]");
		}
		SmbFile smbFile = new SmbFile(url, authentication);
		return smbFile.getInputStream();
	}

	public boolean storeFile(String url, InputStream inputStream) throws IOException {
		if (log.isDebugEnabled())
			log.debug("storeFile path[" + url + "]");
		SmbFile smbFile = new SmbFile(url, authentication);
		SmbFileOutputStream smbout = new SmbFileOutputStream(smbFile, false);
		byte[] buf = new byte[512 * 1024];
		int numRead;
		while ( (numRead = inputStream.read(buf)) >= 0)
			smbout.write(buf, 0, numRead);
		smbout.close();
		return true;
	}

	public List<SmbFile> listFiles(String url) throws SmbException, MalformedURLException  {
		List<SmbFile> fileList = new ArrayList<SmbFile>();
		SmbFile dir = new SmbFile(url, authentication);
		for (SmbFile f : dir.listFiles()){
			fileList.add(f);
		}
		return fileList;
	}	

	public boolean isExist(String url) throws Exception {
		SmbFile sFile = new SmbFile(url, authentication);
		return sFile.exists();
	}

	public boolean delete(String url) throws Exception {
		SmbFile sFile = new SmbFile(url, authentication);
		try {
			sFile.delete();
		} catch(SmbException e) {
			return false;
		}
		return true;
	}

	public boolean rename(String fromUrl, String toUrl) throws Exception {
		SmbFile sFile = new SmbFile(fromUrl, authentication);
		SmbFile renamedFile = new SmbFile(toUrl, authentication);
		try {
			sFile.renameTo(renamedFile);
		} catch (SmbException e) {
			return false;
		}
		return true;
	}

	public NtlmPasswordAuthentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(NtlmPasswordAuthentication authentication) {
		this.authentication = authentication;
	}
}
