package org.harctoolbox.jgirs;

import java.util.List;

/**
 *
 */
public interface ICommandLine {
    public List<String> eval(String line) throws Exception; // FIXME

    public String getAppName();

    public String getHistoryFile();

    public String getPrompt();

    public boolean isQuitRequested();

    public List<String> getCommandNames(boolean sort);

    public List<String> getSubCommandNames(String command, boolean sort);
}
