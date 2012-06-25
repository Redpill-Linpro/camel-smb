package com.redpill_linpro.component.smb;

import org.apache.camel.component.file.GenericFile;

public class SmbGenericFile<T> extends GenericFile<T> {
	
	@Override
    public char getFileSeparator() {
        return '/';
    }

}
