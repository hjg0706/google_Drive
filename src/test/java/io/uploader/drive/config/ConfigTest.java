/*
 * Copyright 2014 Loic Merckel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.uploader.drive.config;

import io.uploader.drive.config.proxy.HasProxySettings;
import io.uploader.drive.config.proxy.Proxy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.uploader.drive.config.Configuration;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ConfigTest {
	
	final private static Logger logger = LoggerFactory.getLogger(ConfigTest.class);
	private File configSettingFile = null ;
	private static boolean hasConfigBeenLoaded = false ;
	
	
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
    
    	
    @Before
    public void init() throws IOException {

    	// generate the test config file
    	File template = new File(new ConfigTest().getClass().getResource("/driveuploader-settings-test.xml").getFile()) ;
    	configSettingFile = tmpFolder.newFile() ;
    	FileUtils.copyFile(template, configSettingFile) ;
    	assertFalse (configSettingFile == null) ;
    	assertTrue (io.uploader.drive.util.FileUtils.getFileAttr(Paths.get(configSettingFile.getPath())).size() > 0) ;
    	
    	// Check the default values before loading
    	if (!hasConfigBeenLoaded)
    	{
	    	assertTrue (80 == Configuration.INSTANCE.getHttpProxySettings().getPort()) ;
	    	assertTrue (443 == Configuration.INSTANCE.getHttpsProxySettings().getPort()) ;
    	}
    	
		// load the test settings
		try 
		{
			Configuration.INSTANCE.load(configSettingFile.getPath()) ;
			hasConfigBeenLoaded = true ;
		} 
		catch (ConfigurationException e) 
		{
			logger.error("Error occurred while loading the cinfiguration file", e);
			assertTrue(false) ;
		}
    }
    
    
    @Test
    public void shouldHaveHttpProxy() {
    	verifyProxy (Configuration.INSTANCE.getHttpProxySettings(), false, "host-http", 9000, "user-http", "password-http") ;
    }
    
    
    @Test
    public void shouldHaveHttpsProxy() {
    	verifyProxy (Configuration.INSTANCE.getHttpsProxySettings(), false, "host-https", 9001, "user-https", "password-https") ;
    }
    
    
    private void verifyProxy (HasProxySettings proxySettings, boolean isActivate, String host, int port, String username, String password)
    {
		assertTrue (proxySettings.isActive() == isActivate) ;
		assertTrue (username.equals(proxySettings.getUsername())) ;
		assertTrue (password.equals(proxySettings.getPassword())) ;
		assertTrue (host.equals(proxySettings.getHost())) ;
		assertTrue (port == proxySettings.getPort()) ;
    }
    
    
    @Test
    public void shouldUpdateHttpProxy() throws IOException {
    	
    	verifyProxy (Configuration.INSTANCE.getHttpProxySettings(), false, "host-http", 9000, "user-http", "password-http") ;
		
    	byte[] initFileContents = FileUtils.readFileToByteArray (configSettingFile) ;

		boolean activated = true ;
		String host = "host.of.the.new.proxy" ;
		String password = "the*new_password" ;
		String username = "the new user name" ;
		int port = 8567 ;
		Proxy newProxy = new Proxy.Builder ("http")
			.setActivated(activated)
			.setHost(host)
			.setPassword(password)
			.setUsername(username)
			.setPort(port).build() ;
		
		Configuration.INSTANCE.updateProxy(newProxy);
		
		verifyProxy (Configuration.INSTANCE.getHttpProxySettings(), activated, host, port, username, password) ;
		
		byte[] updatedFileContents = FileUtils.readFileToByteArray (configSettingFile) ;
		assertFalse(Arrays.equals(updatedFileContents, initFileContents)) ;
    }
    
    
    @Test
    public void shouldUpdateHttpsProxy() throws IOException {
    	
    	verifyProxy (Configuration.INSTANCE.getHttpsProxySettings(), false, "host-https", 9001, "user-https", "password-https") ;
    	
    	byte[] initFileContents = FileUtils.readFileToByteArray (configSettingFile) ;

		boolean activated = true ;
		String host = "host.of.the.new.secured.proxy" ;
		String password = "the*new_password*https" ;
		String username = "the new user name for https" ;
		int port = 8577 ;
		Proxy newProxy = new Proxy.Builder ("https")
			.setActivated(activated)
			.setHost(host)
			.setPassword(password)
			.setUsername(username)
			.setPort(port).build() ;
		
		Configuration.INSTANCE.updateProxy(newProxy);
		
		verifyProxy (Configuration.INSTANCE.getHttpsProxySettings(), activated, host, port, username, password) ;
		
		byte[] updatedFileContents = FileUtils.readFileToByteArray (configSettingFile) ;
		assertFalse(Arrays.equals(updatedFileContents, initFileContents)) ;
    }
}
