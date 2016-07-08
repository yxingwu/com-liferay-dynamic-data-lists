/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.dynamic.data.lists.internal.upgrade.v1_0_0;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.upgrade.AutoBatchPreparedStatementUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Pedro Queiroz
 */
public class UpgradeRecordGroupId extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		updateRecordGroupId();
	}

	protected void updateRecordGroupId() throws Exception {
		String sql = "SELECT DDLRecordSet.groupId, DDLRecord.recordId " 
				+ "FROM DDLRecord INNER JOIN DDLRecordSet "
				+ "ON DDLRecord.recordSetId = DDLRecordSet.recordSetId "
				+ "WHERE DDLRecord.groupId != DDLRecordSet.groupId;";

		try (PreparedStatement ps1 = connection.prepareStatement(sql);
				ResultSet rs = ps1.executeQuery();
				PreparedStatement ps2 = AutoBatchPreparedStatementUtil.concurrentAutoBatch(connection,
						"update DDLRecord set groupId = ? where recordId = ?");) {
			while (rs.next()) {
				long groupId = rs.getLong("groupId");
				long recordId = rs.getLong("recordId");

				ps2.setLong(1, groupId);
				ps2.setLong(2, recordId);

				ps2.addBatch();
			}

			ps2.executeBatch();
		}
	}
}