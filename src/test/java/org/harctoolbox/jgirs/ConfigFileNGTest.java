/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.jgirs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.devslashlirc.LircHardware;
import org.harctoolbox.harchardware.HarcHardwareException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author bengt
 */
public class ConfigFileNGTest {

    private final static String configFilePath = "src/main/config/jgirs_config.xml";
    private final static String devSlashLircPath = "/local/lib64/libdevslashlirc.so";

    @BeforeClass
    public static void setUpClass() throws Exception {
        LircHardware.loadLibrary(new File(devSlashLircPath));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private ConfigFile instance = null;

    public ConfigFileNGTest() {
        System.load("/usr/lib64/rxtx/librxtxSerial.so");
        LircHardware.loadLibrary(new File(devSlashLircPath));
        try {
            instance = new ConfigFile(configFilePath);
        } catch (IOException | SAXException | NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | HarcHardwareException | ConfigFile.NoSuchRemoteTypeException | ParseException | IrpMasterException ex) {
            Logger.getLogger(ConfigFileNGTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getIrHardwareList method, of class ConfigFile.
     */
    @Test
    public void testGetIrHardware() {
        System.out.println("getIrHardware");
        List<GirsHardware> result = instance.getIrHardware();
        assertEquals(result.size(), 4);
    }

    /**
     * Test of getRemoteCommandsDataBase method, of class ConfigFile.
     */
    @Test
    public void testGetRemoteCommandsDataBase() {
        System.out.println("getRemoteCommandsDataBase");
        RemoteCommandDataBase result = instance.getRemoteCommandsDataBase();
        assertEquals(result.getRemotes().size(), 4);
    }

    /**
     * Test of getModuleList method, of class ConfigFile.
     */
    @Test
    public void testGetModuleList() {
        System.out.println("getModuleList");
        List result = instance.getModuleList();
        assertEquals(result.size(), 1);
    }
}
