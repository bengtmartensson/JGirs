package org.harctoolbox.jgirs;

/**
 *
 *
 */
public interface Parameter {

    public String getName();

    public String getDocumentation();

    public void set(String value);

    public String get();
}
