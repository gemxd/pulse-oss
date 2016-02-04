/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package com.pivotal.gemfire.tools.pulse.testbed.driver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.pivotal.gemfire.tools.pulse.testbed.GemFireDistributedSystem.Locator;
import com.pivotal.gemfire.tools.pulse.testbed.GemFireDistributedSystem.Peer;
import com.pivotal.gemfire.tools.pulse.testbed.GemFireDistributedSystem.Region;
import com.pivotal.gemfire.tools.pulse.testbed.GemFireDistributedSystem.Server;
import com.pivotal.gemfire.tools.pulse.testbed.TestBed;

public class PulseUITest {

  private WebDriver driver;
  private TestBed testBed;
  private final String testBedPropertyfile = System.getProperty("pulse.propMockDataUpdaterFile");  
  private static String pulseURL = System.getProperty("pulse.url");
  private static String path =System.getProperty("pulse.war");;
  
  private static final String userName = "admin";
  private static final String pasword = "admin";
  
  private static final String DATA_VIEW_LABEL = "Data View";
  private static final String CLUSTER_VIEW_MEMBERS_ID = "clusterTotalMembersText";
  private static final String CLUSTER_VIEW_SERVERS_ID = "clusterServersText";
  private static final String CLUSTER_VIEW_LOCATORS_ID = "clusterLocatorsText";
  private static final String CLUSTER_VIEW_REGIONS_ID = "clusterTotalRegionsText";
  
  private static Tomcat tomcat = null;
  private static WebDriver initdriver = null;
  
  @BeforeClass
  public static void setUpTomcat(){
    try {      
      String host = InetAddress.getLocalHost().getHostAddress();
      int port = 8080;      
      String context = "/pulse";      
      //System.setProperty("pulse.propMockDataUpdaterClass", "com.pivotal.gemfire.tools.pulse.testbed.PropMockDataUpdater");      
      tomcat = TomcatHelper.startTomcat(host, port, context, path);   
      pulseURL = "http://" + host  + ":" + port + context;
      Thread.sleep(1000); //wait till tomcat settles down
      initdriver = new FirefoxDriver();
      loginToPulse(initdriver, userName, pasword);
      Thread.sleep(5000); //wait till pulse starts polling threads...
      initdriver.close();
    } catch (FileNotFoundException e) {      
      e.printStackTrace();
      Assert.fail("Error " +e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail("Error " +e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("Error " +e.getMessage());
    }        
  }
  

  @Before  
  public void setUp() {
    try {
      testBed = new TestBed(testBedPropertyfile,true);
    } catch (FileNotFoundException e) {      
      e.printStackTrace();
      Assert.fail("Error " +e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail("Error " +e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("Error " +e.getMessage());
    }    
        
    driver = new FirefoxDriver();
    driver.manage().window().maximize();//required to make all elements visible
    loginToPulse();    
  }
  
  private void loginToPulse() {    
    loginToPulse(driver, userName, pasword);
  }

  private static void loginToPulse(WebDriver driver, String userName,String password){
    driver.get(pulseURL);    
    WebElement userNameElement = driver.findElement(By.id("user_name"));
    WebElement passwordElement = driver.findElement(By.id("user_password"));
    userNameElement.sendKeys(userName);
    passwordElement.sendKeys(password);
    passwordElement.submit();
    WebElement userNameOnPulsePage = (new WebDriverWait(driver, 10))
        .until(new ExpectedCondition<WebElement>() {
          @Override
          public WebElement apply(WebDriver d) {
            return d.findElement(By.id("userName"));
          }
        });
    Assert.assertNotNull(userNameOnPulsePage);
  }
  
  
  private void searchByLinkAndClick(String linkText){
    WebElement  dataViewButton= By.linkText(linkText).findElement(driver);
    Assert.assertNotNull(dataViewButton);   
    dataViewButton.click();
  }
  
  private void searchByIdAndClick(String id){
    WebElement  element = driver.findElement(By.id(id));
    Assert.assertNotNull(element);
    element.click();    
  }
  
  private void searchByXPathAndClick(String xpath){    
    WebElement  element = driver.findElement(By.xpath(xpath));
    Assert.assertNotNull(element);
    element.click();    
  }
  
  private void waitForElementByClassName(final String className, int seconds){
    WebElement linkTextOnPulsePage1 = (new WebDriverWait(driver, seconds))
    .until(new ExpectedCondition<WebElement>() {
      @Override
      public WebElement apply(WebDriver d) {
        return d.findElement(By.className(className));
      }
    });
    Assert.assertNotNull(linkTextOnPulsePage1);
  }
  
  private void waitForElementById(final String id, int seconds){
    WebElement element = (new WebDriverWait(driver, 10))
    .until(new ExpectedCondition<WebElement>() {
      @Override
      public WebElement apply(WebDriver d) {
        return d.findElement(By.id(id));
      }
    });
    Assert.assertNotNull(element);
  }
  
  @Test
  public void testClusterViewTopRibbon() {
    List<Server> servers = testBed.getRootDs().getServers();
    List<Locator> locators = testBed.getRootDs().getLocators();
    List<Peer> peers = testBed.getRootDs().getPeers();
    List<Region> regions = testBed.getRootDs().getRegions();
    int totalMembers = servers.size() + locators.size() + peers.size();
    int clusterMembers = Integer.parseInt(driver.findElement(
        By.id(CLUSTER_VIEW_MEMBERS_ID)).getText());
    int clusterServers = Integer.parseInt(driver.findElement(
        By.id(CLUSTER_VIEW_SERVERS_ID)).getText());
    int clusterLocators = Integer.parseInt(driver.findElement(
        By.id(CLUSTER_VIEW_LOCATORS_ID)).getText());
    int clusterRegions = Integer.parseInt(driver.findElement(
        By.id(CLUSTER_VIEW_REGIONS_ID)).getText());
    Assert.assertEquals(totalMembers, clusterMembers);
    Assert.assertEquals(servers.size(), clusterServers);
    Assert.assertEquals(locators.size(), clusterLocators);
    Assert.assertEquals(regions.size(), clusterRegions);
  }  


  @Test
  public void testDataViewRegionProperties() {
    searchByLinkAndClick(DATA_VIEW_LABEL);
    waitForElementByClassName("pointDetailsPadding",10);    
    searchByIdAndClick("btngridIcon");
    
    for(int i=1;i<testBed.getRootDs().getRegions().size();i++){
      searchByIdAndClick(""+i);
      String regionName1 = driver.findElement(By.id("regionPath")).getText();
      int sepIndex1 = regionName1.lastIndexOf('/');
      if (sepIndex1 >= 0) {
        regionName1 = regionName1.substring(sepIndex1 + 1);
      }
      @SuppressWarnings("rawtypes")
      List regionMemberscount1 = testBed.getRootDs().getRegion(regionName1)
          .getMembers();
      int regionEntCount1 = testBed.getRootDs().getRegion(regionName1)
          .getEntryCount();
      int regionMembers1 = Integer.parseInt(driver.findElement(
          By.id("regionMembers")).getText());
      int regionEntryCount1 = Integer.parseInt(driver.findElement(
          By.id("regionEntryCount")).getText());
      Assert.assertEquals(regionMemberscount1.size(), regionMembers1);
      Assert.assertEquals(regionEntCount1, regionEntryCount1);
    }
  }

  
  @Test
  public void testMemberViewRegions() {
    
    searchByLinkAndClick(DATA_VIEW_LABEL);
    waitForElementByClassName("pointDetailsPadding",10);    
    searchByXPathAndClick("//div[@title='peer1']");    
    waitForElementById("memberRegionsCount",10);    
    
    List<Server> servers = testBed.getRootDs().getServers();
    // List<Locator> locators = testBed.getRootDs().getLocators();
    List<Peer> peers = testBed.getRootDs().getPeers();    

    String prevSelectedMember = "peer1";
    
    for (Peer p : peers) {
      String peer = p.getName();
      System.out.println("Checking regions mapping for member " + peer);
      WebElement comboBox = driver.findElement(By.linkText(prevSelectedMember));
      comboBox.click();                 
      WebElement comboList = driver.findElement(By.id("clusterMembersContainer"));     
      WebElement selectedMember = comboList.findElement(By.linkText(peer));
      selectedMember.click();
      timeout();
      String peername = driver.findElement(By.id("memberName")).getText();      
      List<Region> peerRegionscount = testBed.getRootDs().getRegions(peer);
      int peerRegions = Integer.parseInt(driver.findElement(
          By.id("memberRegionsCount")).getText());
      Assert.assertEquals(peerRegionscount.size(), peerRegions);
      prevSelectedMember = peername;
    }
    
    for (Server s : servers) {
      String server = s.getName();
      System.out.println("Checking regions mapping for server " + server);
      WebElement comboBox = driver.findElement(By.linkText(prevSelectedMember));
      comboBox.click();                 
      WebElement comboList = driver.findElement(By.id("clusterMembersContainer"));     
      WebElement selectedMember = comboList.findElement(By.linkText(server));
      selectedMember.click();
      timeout();
      String peername = driver.findElement(By.id("memberName")).getText();      
      List<Region> serverRegionscount = testBed.getRootDs().getRegions(server);
      int serverRegions = Integer.parseInt(driver.findElement(
          By.id("memberRegionsCount")).getText());
      Assert.assertEquals(serverRegionscount.size(), serverRegions);
      prevSelectedMember = peername;            
    }
    /*
    for (Locator l : locators) {      
      String locator = l.getName();
      System.out.println("Checking regions mapping for locator " + locator);
      WebElement comboBox = driver.findElement(By.linkText(prevSelectedMember));
      comboBox.click();                 
      WebElement comboList = driver.findElement(By.id("clusterMembersContainer"));     
      WebElement selectedMember = comboList.findElement(By.linkText(locator));
      selectedMember.click();
      timeout();
      String peername = driver.findElement(By.id("memberName")).getText();      
      List<Region> locatorRegionscount = testBed.getRootDs().getRegions(locator);
      int locatorRegions = Integer.parseInt(driver.findElement(
          By.id("memberRegionsCount")).getText());
      Assert.assertEquals(locatorRegionscount.size(), locatorRegions);
      prevSelectedMember = peername;
    }*/
  }

  public void timeout() {
    WebElement memberNameOnPulsePage = (new WebDriverWait(driver, 10))
        .until(new ExpectedCondition<WebElement>() {
          @Override
          public WebElement apply(WebDriver d) {
            return d.findElement(By.id("memberName"));
          }
        });
    Assert.assertNotNull(memberNameOnPulsePage);    
  }  

  @After
  public void closeSession() {      
    driver.close();
  }
  
  @AfterClass
  public static void stopTomcat(){
    try {
      if(tomcat!=null){
        tomcat.stop();
        tomcat.destroy();
      }
    } catch (LifecycleException e) {     
      e.printStackTrace();
    }
  }
}
