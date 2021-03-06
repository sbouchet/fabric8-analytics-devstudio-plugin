/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/

package com.redhat.fabric8analytics.lsp.eclipse.core.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.security.storage.StorageException;

import com.redhat.fabric8analytics.lsp.eclipse.core.Fabric8AnalysisLSCoreActivator;
import com.redhat.fabric8analytics.lsp.eclipse.core.Fabric8AnalysisPreferences;
import com.redhat.fabric8analytics.lsp.eclipse.core.ThreeScaleAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.ThreeScaleAPIProvider;
import com.redhat.fabric8analytics.lsp.eclipse.core.data.AnalyticsAuthData;
import com.redhat.fabric8analytics.lsp.eclipse.core.data.ThreeScaleData;

public class AnalyticsAuthService {

	private static final AnalyticsAuthService INSTANCE = new AnalyticsAuthService();

	public static AnalyticsAuthService getInstance() {
		return INSTANCE;
	}

	private AnalyticsAuthService() {
	}

	/**
	 * Returns auth data - 3scale data. 
	 * If user was not logged before, null is returned.
	 * 
	 * @return 3scale data (3scale data)
	 * @throws ThreeScaleAPIException 
	 */
	public AnalyticsAuthData getAnalyticsAuthData(IProgressMonitor progressMonitor) throws StorageException{
		SubMonitor monitor = getSubMonitor(progressMonitor);
		monitor.setWorkRemaining(2);
		monitor.setTaskName("Check Openshift.io accout status");
		ThreeScaleData threeScaleData = Fabric8AnalysisPreferences.getInstance().getThreeScaleData();
		if(threeScaleData == null) {
			try {
				threeScaleData = registerThreeScale(monitor);
			} catch (ThreeScaleAPIException e) {
				Fabric8AnalysisLSCoreActivator.getDefault().logError("Unable to get data from 3scale", e);
				return null;
			}
		}
		return new AnalyticsAuthData(threeScaleData);
		
	}

	/**
	 * If anything goes wrong during login process, null is returned
	 * 
	 * @param progressMonitor progressMonitor to report login progress.
	 * @return 3scale data (3scale data) or null if login process fails
	 */
	public AnalyticsAuthData login(IProgressMonitor progressMonitor) throws StorageException{
		SubMonitor monitor = getSubMonitor(progressMonitor);
		monitor.setWorkRemaining(2);
		ThreeScaleData threeScaleData = null;
		
			try {
				threeScaleData = registerThreeScale(monitor);
			} catch (ThreeScaleAPIException e) {
				Fabric8AnalysisLSCoreActivator.getDefault().logError("Unable to get data from 3scale", e);
				return null;
			}
		AnalyticsAuthData analyticsAuthData = new AnalyticsAuthData(threeScaleData);
		return analyticsAuthData;
	}
	
	private ThreeScaleData registerThreeScale(SubMonitor monitor) throws ThreeScaleAPIException, StorageException {
		monitor.setWorkRemaining(1);
		monitor.setTaskName("Get ThreeScale data");
		Fabric8AnalysisPreferences preferences = Fabric8AnalysisPreferences.getInstance();
		ThreeScaleData threeScaleData = new ThreeScaleAPIProvider().register3Scale();
		preferences.setProdURL(threeScaleData.getProd());
		preferences.setStageURL(threeScaleData.getStage());
		preferences.setUserKey(threeScaleData.getUserKey());
		return threeScaleData;
	}
	
	private SubMonitor getSubMonitor(IProgressMonitor progressMonitor) {
		if(progressMonitor == null) {
			progressMonitor = new NullProgressMonitor();
		}
		return SubMonitor.convert(progressMonitor);
	}
}
