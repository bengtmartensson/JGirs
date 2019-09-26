package org.harctoolbox.jgirs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import org.harctoolbox.devslashlirc.LircHardware;
import org.harctoolbox.girr.GirrException;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.ircore.IrCoreException;
import org.harctoolbox.irp.IrpException;
import org.harctoolbox.irp.IrpParseException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 * TODO: Replace by something more portable.
 */
public class ConfigFileNGTest {

    private final static String NATIVE_DIR = "native/Linux-amd64";
    private final static String configFilePath = "src/main/config/jgirs_config.xml"; // FIXME
    private final static String devSlashLircPath = NATIVE_DIR + "/libdevslashlirc.so"; // FIXME
    private final static String RXTX_SERIAL_PATH = NATIVE_DIR + "/librxtxSerial.so";

    @BeforeClass
    public static void setUpClass() throws Exception {
        LircHardware.loadLibrary(new File(devSlashLircPath));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private ConfigFile instance = null;

    public ConfigFileNGTest() {
        try {
            System.load(new File(RXTX_SERIAL_PATH).getCanonicalPath());
            LircHardware.loadLibrary(new File(devSlashLircPath));
            instance = new ConfigFile(configFilePath);
        } catch (JGirsException | IOException | SAXException | NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | HarcHardwareException | ParseException | IrpParseException | GirrException | IrpException | IrCoreException ex) {
            instance = null;
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
    @Test(enabled = false)
    public void testGetIrHardware() {
        System.out.println("getIrHardware");
        if (instance == null)
            fail();
        Collection<GirsHardware> result = instance.getIrHardware();
        assertEquals(result.size(), 5);
    }

    /**
     * Test of getRemoteCommandsDataBase method, of class ConfigFile.
     */
    @Test(enabled = false)
    public void testGetRemoteCommandsDataBase() {
        System.out.println("getRemoteCommandsDataBase");
        if (instance == null)
            fail();
        RemoteCommandDataBase result = instance.getRemoteCommandsDataBase();
        assertEquals(result.getRemotes().size(), 4);
    }

    /**
     * Test of getModuleList method, of class ConfigFile.
     */
    @Test(enabled = false)
    public void testGetModuleList() {
        System.out.println("getModuleList");
        if (instance == null)
            fail();
        List result = instance.getModuleList();
        assertEquals(result.size(), 0);
    }
}
