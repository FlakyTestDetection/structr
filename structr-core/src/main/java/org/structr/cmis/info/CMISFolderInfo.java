package org.structr.cmis.info;

import org.structr.cmis.info.CMISObjectInfo;

/**
 * Optional interface for CMIS support in Structr core.
 *
 * @author Christian Morgner
 */
public interface CMISFolderInfo extends CMISObjectInfo {

	public String getParentId();
	public String getPath();

}