package com.redpill_linpro.component.smb;

import java.io.IOException;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileConsumer;
import org.apache.camel.component.file.GenericFileEndpoint;
import org.apache.camel.component.file.GenericFileOperations;
import org.apache.camel.util.FileUtil;

public class SmbConsumer extends GenericFileConsumer<SmbFile>{

	private String endpointPath;
	private String currentRelativePath = "";

	public SmbConsumer(GenericFileEndpoint<SmbFile> endpoint, Processor processor, GenericFileOperations<SmbFile> operations) {
		super(endpoint, processor, operations);
		this.endpointPath = endpoint.getConfiguration().getDirectory();
	}
	
	@Override
	protected boolean pollDirectory(String fileName, List<GenericFile<SmbFile>> fileList, int depth) {
		
		if (log.isDebugEnabled()) {
			log.debug("pollDirectory() running. My delay is [" + this.getDelay() + "] and my strategy is [" + this.getPollStrategy().getClass().toString() + "]");
			log.debug("pollDirectory() fileName[" + fileName + "]");
		}
		
		List<SmbFile> smbFiles;
		boolean currentFileIsDir = false;
		smbFiles = operations.listFiles(fileName);
		for (SmbFile smbFile : smbFiles) {
			if (!canPollMoreFiles(fileList)) {
				return false;
			}
			try {
				if (smbFile.isDirectory()) {
					currentFileIsDir = true;
				}
				else {
					currentFileIsDir = false;
				}
			} catch (SmbException e1) {
				log.warn("Caught SmbException: " + e1.getMessage());
			}
			if (currentFileIsDir) { 
				if (endpoint.isRecursive()) {
					currentRelativePath = smbFile.getName().split("/")[0] + "/";
					pollDirectory(fileName + smbFile.getName(), fileList, depth++);
				}
				else {
					currentRelativePath = "";
				}
			}
			else {
				try {
					GenericFile<SmbFile> genericFile = asGenericFile(fileName, smbFile);
					if (isValidFile(genericFile, false, smbFiles)) {
						// matched file so add
						fileList.add(asGenericFile(fileName, smbFile));
					}
				} catch (IOException e) {
					log.warn("Caught IOException: " + e.getMessage());
				}
			}
		}
		return true;
	}

	//TODO: this needs some checking!
	private GenericFile<SmbFile> asGenericFile(String path, SmbFile file) throws IOException{
		SmbGenericFile<SmbFile> answer = new SmbGenericFile<SmbFile>();
		answer.setAbsoluteFilePath(path + answer.getFileSeparator() + file.getName());
		answer.setAbsolute(true);
		answer.setEndpointPath(endpointPath);
		answer.setFileNameOnly(file.getName());
		answer.setFileLength(file.getContentLength());
		answer.setFile(file);
		answer.setLastModified(file.getLastModified());
		answer.setFileName(currentRelativePath + file.getName());
		answer.setRelativeFilePath(file.getName());
		if (log.isDebugEnabled()) {
			log.debug("asGenericFile():");
			log.debug("absoluteFilePath[" + answer.getAbsoluteFilePath() +"] endpointpath[" + answer.getEndpointPath() + "] filenameonly["+ answer.getFileNameOnly() +"] filename[" + answer.getFileName() + "] relativepath[" + answer.getRelativeFilePath() + "]");
			
		}
		return answer;
	}

	@Override
    protected boolean isMatched(GenericFile<SmbFile> file, String doneFileName, List<SmbFile> files) {
        String onlyName = FileUtil.stripPath(doneFileName);

        for (SmbFile f : files) {
            if (f.getName().equals(onlyName)) {
                return true;
            }
        }

        log.trace("Done file: {} does not exist", doneFileName);
        return false;
    }

}
