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

import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

/** 
 * Factory interface for creating jcifs API objects. 
 *
 */
public interface SmbApiFactory {

	SmbFile createSmbFile(String url, NtlmPasswordAuthentication authentication) throws MalformedURLException, SmbException;

	SmbFileOutputStream createSmbFileOutputStream(SmbFile smbFile, boolean b) throws SmbException, MalformedURLException, UnknownHostException;

}
