package org.harctoolbox.jgirs;

import java.io.IOException;
import java.util.List;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.harchardware.HarcHardwareException;

/**
 * Defines the functions a command line program must implement.
 */
public interface ICommandLine {
    public List<String> eval(String line) throws JGirsException, IOException, HarcHardwareException, IrpMasterException;

    public String getAppName();

    public String getHistoryFile();

    public String getPrompt();

    public boolean isQuitRequested();

    public List<String> getCommandNames(boolean sort);

    public List<String> getSubCommandNames(String command, boolean sort);
}
