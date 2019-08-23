package org.harctoolbox.jgirs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.harctoolbox.girr.Command;
import org.harctoolbox.girr.GirrException;
import org.harctoolbox.girr.RemoteSet;
import org.harctoolbox.irp.IrpParseException;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class CsvImporterNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final CsvImporter instance;

    public CsvImporterNGTest() throws IOException, IrpParseException {
        Command.setIrpMaster("/usr/local/share/irscrutinizer/IrpProtocols.xml");
        instance = new CsvImporter();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of parseRemoteSet method, of class CsvImporter.
     * @throws java.io.FileNotFoundException
     * @throws org.harctoolbox.girr.GirrException
     */
    @Test
    public void testParseRemoteSet() throws IOException, GirrException {
        System.out.println("parseRemoteSet");
        InputStreamReader reader = new InputStreamReader(new FileInputStream("/home/bengt/harctoolbox/irdb/codes/Philips/Unknown_32PFL5403D/0,-1.csv"), "US-ASCII");
        String remoteName = "blah";
        String source = "/home/bengt/harctoolbox/irdb/codes/Philips/Unknown_32PFL5403D/0,-1.csv";
        RemoteSet result = instance.parseRemoteSet(remoteName, source, reader);
        assertEquals(result.getRemote(remoteName).getName(), remoteName);
    }
}
