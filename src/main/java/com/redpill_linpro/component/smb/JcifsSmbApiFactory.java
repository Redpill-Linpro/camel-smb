package com.redpill_linpro.component.smb;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

/**
 * Default implementation of the the {@link SmbApiFactory}
 * 
 * @author Pontus Ullgren
 *
 */
public class JcifsSmbApiFactory implements SmbApiFactory {

	public SmbFile createSmbFile(String url,
			NtlmPasswordAuthentication authentication)
			throws MalformedURLException, SmbException {
		return new SmbFile(url, authentication);
	}

	public SmbFileOutputStream createSmbFileOutputStream(SmbFile smbFile,
			boolean b) throws SmbException, MalformedURLException, UnknownHostException {
		return new SmbFileOutputStream(smbFile, b);
	}

}
