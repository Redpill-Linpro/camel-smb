package com.redpill_linpro.component.smb;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

/** 
 * Factory interface for creating jcifs API objects. 
 * 
 * @author Pontus Ullgren
 *
 */
public interface SmbApiFactory {

	SmbFile createSmbFile(String url, NtlmPasswordAuthentication authentication) throws MalformedURLException, SmbException;

	SmbFileOutputStream createSmbFileOutputStream(SmbFile smbFile, boolean b) throws SmbException, MalformedURLException, UnknownHostException;

}
